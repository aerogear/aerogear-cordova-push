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
using System.Threading.Tasks;

namespace AeroGear.Push
{
    /// <summary>
    /// Base class for registration implementors need to implment how to get the Channel and ChannelStore
    /// </summary>
    public abstract class Registration
    {
        private const string CHANNEL_KEY = "Channel";
        public const string PUSH_ID_KEY = "aerogear-push-id";
        private const string FILE_NAME = "push-config.json";

        public event EventHandler<PushReceivedEvent> PushReceivedEvent;

        public async Task<string> Register()
        {
            var config = await LoadConfigJson(FILE_NAME);
            return await Register(config, CreateUPSHttpClient(config));
        }

        public async Task<string> Register(PushConfig pushConfig)
        {
            return await Register(pushConfig, CreateUPSHttpClient(pushConfig));
        }

        public async Task<string> Register(IUPSHttpClient client)
        {
            return await Register(await LoadConfigJson(FILE_NAME), client);
        }

        public async Task<string> Register(PushConfig pushConfig, IUPSHttpClient client)
        {
            Installation installation = CreateInstallation(pushConfig);
            ILocalStore store = CreateChannelStore();
            string channelUri = await ChannelUri();
            var token = pushConfig.VariantId + channelUri;
            if (!token.Equals(store.Read(CHANNEL_KEY)))
            {
                installation.deviceToken = channelUri;
                await client.Register(installation);

                store.Save(CHANNEL_KEY, token);
            }
            return installation.deviceToken;
        }

        /// <summary>
        /// Update the categories 
        /// </summary>
        /// <param name="categories">The categories for this device</param>
        /// <returns>Void</returns>
        public async Task SetCategories(List<string> categories)
        {
            var pushConfig = await LoadConfigJson(FILE_NAME);
            pushConfig.Categories = categories;
            await UpdateConfig(pushConfig, CreateUPSHttpClient(pushConfig));
        }

        /// <summary>
        /// update the alias
        /// </summary>
        /// <param name="alias">The alias for this device</param>
        /// <returns>Void</returns>
        public async Task SetAlias(string alias)
        {
            var pushConfig = await LoadConfigJson(FILE_NAME);
            pushConfig.Alias = alias;
            await UpdateConfig(pushConfig, CreateUPSHttpClient(pushConfig));
        }

        /// <summary>
        /// Update the entire configuration
        /// </summary>
        /// <param name="pushConfig">the configuration to update</param>
        /// <returns>Void</returns>
        public async Task UpdateConfig(PushConfig pushConfig)
        {
            await UpdateConfig(pushConfig, CreateUPSHttpClient(pushConfig));
        }

        public async Task UpdateConfig(PushConfig pushConfig, IUPSHttpClient client)
        {
            var installation = CreateInstallation(pushConfig);
            installation.deviceToken = CreateChannelStore().Read(CHANNEL_KEY).Substring(pushConfig.VariantId.Length);
            await client.Register(installation);
        }

        public async Task SendMetricWhenAppLaunched(PushConfig pushConfig)
        {
            ILocalStore store = CreateChannelStore();
            var pushIdentifier = store.Read(PUSH_ID_KEY);
            var client = CreateUPSHttpClient(pushConfig);
            await client.SendMetrics(pushIdentifier);
            store.Save(PUSH_ID_KEY, null);
        }

        public async Task SendMetricWhenAppLaunched(PushConfig pushConfig, string pushIdentifier)
        {
            var client = CreateUPSHttpClient(pushConfig);
            await client.SendMetrics(pushIdentifier);
        }

        protected void OnPushNotification(string message, IDictionary<string, string> data)
        {
            EventHandler<PushReceivedEvent> handler = PushReceivedEvent;
            if (data != null && data.ContainsKey(PUSH_ID_KEY))
            {
                CreateChannelStore().Save(PUSH_ID_KEY, data[PUSH_ID_KEY]);
            }
            if (handler != null)
            {
                handler(this, new PushReceivedEvent(new PushNotification() {Message = message, Data = data}));
            }
        }

        private IUPSHttpClient CreateUPSHttpClient(PushConfig pushConfig)
        {
            return new UPSHttpClient(pushConfig.UnifiedPushUri, pushConfig.VariantId, pushConfig.VariantSecret);
        }

        /// <summary>
        /// Create an installation with as much details as posible so it's easy to find it again in UPS
        /// </summary>
        /// <param name="pushConfig">Push configuration to base the installation off</param>
        /// <returns>Installation filled with the details</returns>
        protected abstract Installation CreateInstallation(PushConfig pushConfig);

        /// <summary>
        /// Create a target specific ChannelStore
        /// </summary>
        /// <returns>A channel store that works on specified target</returns>
        protected abstract ILocalStore CreateChannelStore();

        /// <summary>
        /// Load the configuration from a file.
        /// </summary>
        /// <param name="filename">the json file to load the config from</param>
        /// <returns>The loaded push configuration</returns>
        public abstract Task<PushConfig> LoadConfigJson(string filename);

        /// <summary>
        /// Register with the push network and return the current channel uri
        /// </summary>
        /// <returns>current channel uri</returns>
        protected abstract Task<string> ChannelUri();
    }
}
