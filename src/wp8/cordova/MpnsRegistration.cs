/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

using System;
using System.Collections.Generic;
using System.IO;
using System.Runtime.Serialization.Json;
using System.Threading.Tasks;
using System.Windows;
using Microsoft.Phone.Notification;

namespace AeroGear.Push
{
    /// <summary>
    ///     Mpns based version
    /// </summary>
    public class MpnsRegistration : Registration
    {
        private void PushChannel_ShellToastNotificationReceived(object sender, NotificationEventArgs e)
        {
            var message = e.Collection["wp:Text1"];
            IDictionary<string, string> data = null;
            if (e.Collection.ContainsKey("wp:Param") && e.Collection["wp:Param"] != null)
            {
                data = UrlQueryParser.ParseQueryString(e.Collection["wp:Param"]);
            }
            OnPushNotification(message, data);
        }

        private void PushChannel_ErrorOccurred(object sender, NotificationChannelErrorEventArgs e)
        {
            Deployment.Current.Dispatcher.BeginInvoke(() =>
            {
                MessageBox.Show(string.Format("A push notification {0} error occurred.  {1} ({2}) {3}",
                    e.ErrorType, e.Message, e.ErrorCode, e.ErrorAdditionalData));
            });
        }

        protected override Installation CreateInstallation(PushConfig pushConfig)
        {
            var operatingSystem = Environment.OSVersion.Platform.ToString();
            var osVersion = Environment.OSVersion.Version.ToString();
            var installation = new Installation
            {
                alias = pushConfig.Alias,
                operatingSystem = operatingSystem,
                osVersion = osVersion,
                categories = pushConfig.Categories
            };
            return installation;
        }

        protected override async Task<string> ChannelUri()
        {
            const string channelName = "ToastChannel";

            var channel = HttpNotificationChannel.Find(channelName) ?? new HttpNotificationChannel(channelName);

            var tcs = new TaskCompletionSource<string>();
            channel.ChannelUriUpdated += (s, e) => { tcs.TrySetResult(e.ChannelUri.ToString()); };
            channel.ErrorOccurred += (s, e) => { tcs.TrySetException(new Exception(e.Message)); };

            channel.ShellToastNotificationReceived += PushChannel_ShellToastNotificationReceived;

            channel.Open();
            channel.BindToShellToast();
            return await tcs.Task;
        }

        protected override ILocalStore CreateChannelStore()
        {
            return new LocalStore();
        }

        public override async Task<PushConfig> LoadConfigJson(string filename)
        {
            var serializer = new DataContractJsonSerializer(typeof (PushConfig));
            return (PushConfig) serializer.ReadObject(File.OpenRead(filename));
        }
    }
}