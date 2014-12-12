using System;
using System.Net;
using System.Windows;
using System.Threading.Tasks;
using Microsoft.Phone.Notification;
using System.Collections.Generic;

namespace AeroGear.Push
{
    public class MpnsRegistration : Registration
    {
        private Installation installation;
        private IUPSHttpClient client;
        protected async override Task<string> Register(Installation installation, IUPSHttpClient client)
        {
            this.installation = installation;
            this.client = client;
            HttpNotificationChannel channel;
            string channelName = "ToastChannel";

            channel = HttpNotificationChannel.Find(channelName);

            if (channel == null)
            {
                channel = new HttpNotificationChannel(channelName);
            }

            var tcs = new TaskCompletionSource<string>();
            channel.ChannelUriUpdated += async (s, e) =>
            {
                ChannelStore channelStore = new ChannelStore();
                if (!e.ChannelUri.ToString().Equals(channelStore.Read()))
                {
                    installation.deviceToken = e.ChannelUri.ToString();
                    await client.register(installation);
                    channelStore.Save(installation.deviceToken);
                    tcs.TrySetResult(installation.deviceToken);
                }
            };
            channel.ErrorOccurred += (s, e) =>
            {
                tcs.TrySetException(new Exception(e.Message));
            };

            channel.ShellToastNotificationReceived += new EventHandler<NotificationEventArgs>(PushChannel_ShellToastNotificationReceived);

            channel.Open();
            channel.BindToShellToast();
            return await tcs.Task;
        }

        private void PushChannel_ShellToastNotificationReceived(object sender, NotificationEventArgs e)
        {
            string message = e.Collection["wp:Text1"];
            IDictionary<string, string> data = null;
            if (e.Collection.ContainsKey("wp:Param") && e.Collection["wp:Param"] != null)
            {
                data = UrlQueryParser.ParseQueryString(e.Collection["wp:Param"]);
            }
            OnPushNotification(message, data);
        }

        private void PushChannel_ErrorOccurred(object sender, NotificationChannelErrorEventArgs e)
        {
            Deployment.Current.Dispatcher.BeginInvoke(new Action(() =>
            {
                MessageBox.Show(String.Format("A push notification {0} error occurred.  {1} ({2}) {3}",
                    e.ErrorType, e.Message, e.ErrorCode, e.ErrorAdditionalData));
            }));
        }

        protected override Installation CreateInstallation(PushConfig pushConfig)
        {
            string operatingSystem = Environment.OSVersion.Platform.ToString();
            string osVersion = Environment.OSVersion.Version.ToString();
            Installation installation = new Installation() { alias = pushConfig.Alias, operatingSystem = operatingSystem, osVersion = osVersion, categories = pushConfig.Categories };
            return installation;
        }
    }
}
