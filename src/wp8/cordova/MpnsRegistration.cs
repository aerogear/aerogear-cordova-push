using System;
using System.Net;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Documents;
using System.Windows.Ink;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Animation;
using System.Windows.Shapes;
using System.Threading.Tasks;
using Microsoft.Phone.Notification;
using Microsoft.Phone.Info;
using System.Collections.Generic;
using System.Text.RegularExpressions;

namespace AeroGear.Push
{
    public class MpnsRegistration : Registration
    {
        private Installation installation;
        private IUPSHttpClient client;
        protected async override Task Register(Installation installation, IUPSHttpClient client)
        {
            this.installation = installation;
            this.client = client;
            HttpNotificationChannel channel;
            string channelName = "ToastChannel";

            await Task.Run(() =>
            {
                channel = HttpNotificationChannel.Find(channelName);

                if (channel == null)
                {
                    channel = new HttpNotificationChannel(channelName);
                }
                channel.ChannelUriUpdated += new EventHandler<NotificationChannelUriEventArgs>(PushChannel_ChannelUriUpdated);
                channel.ErrorOccurred += new EventHandler<NotificationChannelErrorEventArgs>(PushChannel_ErrorOccurred);
                channel.ShellToastNotificationReceived += new EventHandler<NotificationEventArgs>(PushChannel_ShellToastNotificationReceived);

                channel.Open();
                channel.BindToShellToast();
            });
        }

        private void PushChannel_ShellToastNotificationReceived(object sender, NotificationEventArgs e)
        {
            string message = e.Collection["wp:Text1"];
            IDictionary<string, string> data = null;
            if (e.Collection.Keys.Contains("wp:Param") && e.Collection["wp:Param"] != null)
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

        private void PushChannel_ChannelUriUpdated(object sender, NotificationChannelUriEventArgs e)
        {
            ChannelStore channelStore = new ChannelStore();
            if (!e.ChannelUri.ToString().Equals(channelStore.Read()))
            {
                installation.deviceToken = e.ChannelUri.ToString();
                channelStore.Save(e.ChannelUri.ToString());
                client.register(installation);
            }
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
