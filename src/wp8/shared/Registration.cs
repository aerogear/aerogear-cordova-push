﻿using System;
using System.Collections.Generic;
using System.Threading.Tasks;

namespace AeroGear.Push
{
    public abstract class Registration
    {
        public event EventHandler<PushReceivedEvent> PushReceivedEvent;

        public async Task<string> Register(PushConfig pushConfig)
        {
            Installation installation = CreateInstallation(pushConfig);
            return await Register(installation, CreateUPSHttpClient(pushConfig));
        }

        public async Task<string> Register(PushConfig pushConfig, IUPSHttpClient client)
        {
            return await Register(CreateInstallation(pushConfig), client);
        }

        protected void OnPushNotification(string message, IDictionary<string, string> data)
        {
            EventHandler<PushReceivedEvent> handler = PushReceivedEvent;
            if (handler != null)
            {
                handler(this, new PushReceivedEvent(new PushNotification() {message = message, data = data}));
            }
        }

        protected abstract Task<string> Register(Installation installation, IUPSHttpClient iUPSHttpClient);

        private IUPSHttpClient CreateUPSHttpClient(PushConfig pushConfig)
        {
            return new UPSHttpClient(pushConfig.UnifiedPushUri, pushConfig.VariantId, pushConfig.VariantSecret);
        }

        protected abstract Installation CreateInstallation(PushConfig pushConfig);
    }
}
