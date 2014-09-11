/*
 * JBoss, Home of Professional Open Source.
 * Copyright Red Hat, Inc., and individual contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
using System;
using System.Collections.Generic;
using System.Runtime.Serialization;
using WPCordovaClassLib.Cordova.Commands;
using WPCordovaClassLib.Cordova.JSON;

using AeroGear.Push;
using WPCordovaClassLib.Cordova;
using org.jboss.aerogear.cordova.push;
using System.Runtime.Serialization.Json;
using System.IO;
using System.Text;

public class PushPlugin : BaseCommand
{
    public async void register(string unparsedOptions)
    {
        var options = JsonHelper.Deserialize<string[]>(unparsedOptions)[0];
        var config = JsonHelper.Deserialize<PushConfig>(options);

        Registration registration = new MpnsRegistration();
        registration.PushReceivedEvent += HandleNotification;
        await registration.Register(new AeroGear.Push.PushConfig() { UnifiedPushUri = config.UnifiedPushUri, VariantId = config.VariantId, VariantSecret = config.VariantSecret });
        InvokeCustomScript(new ScriptCallback("eval", new string[] { "cordova.require('org.jboss.aerogear.cordova.push.AeroGear.UnifiedPush').successCallback()" }), false);

        PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
        result.KeepCallback = true;
        DispatchCommandResult(result);

        if (P.message != null)
        {
            HandleNotification(new Event() { Alert = P.message, Payload = P.data });
        }
    }

    void HandleNotification(object sender, PushReceivedEvent e)
    {
        HandleNotification(new Event { Alert = e.Args.message, Payload = e.Args.data});
    }

    void HandleNotification(Event pushEvent)
    {
        PluginResult result = new PluginResult(PluginResult.Status.OK, pushEvent);
        result.KeepCallback = true;
        DispatchCommandResult(result);

    }

    [DataContract]
    class PushConfig
    {
        [DataMember(IsRequired = true, Name = "pushServerURL")]
        public Uri UnifiedPushUri { get; set; }

        private string variantId;
        [DataMember(Name = "variantID")]
        public string VariantId
        {
            get
            {
                return variantId != null ? variantId : windows.VariantId;
            }
            set
            {
                variantId = value;
            }
        }

        private string variantSecret;
        [DataMember(Name = "variantSecret")]
        public string VariantSecret
        {
            get
            {
                return variantSecret != null ? variantSecret : windows.VariantSecret;
            }
            set
            {
                variantSecret = value;
            }
        }

        [DataMember]
        public Windows windows { get; set; }

        [DataContract]
        public class Windows
        {
            [DataMember(Name = "variantID")]
            public string VariantId { get; set; }

            [DataMember(Name = "variantSecret")]
            public string VariantSecret { get; set; }
        }

        [DataMember(Name = "categories")]
        public IList<string> Categories { get; set; }

        [DataMember(Name = "alias")]
        public string Alias { get; set; }
    }

    [DataContract]
    class Event
    {
        [DataMember(IsRequired = true, Name = "alert")]
        public string Alert { get; set; }

        [DataMember(Name = "payload")]
        public IDictionary<string, string> Payload { get; set; }
    }
}