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
using System.Diagnostics;
using System.Runtime.Serialization;
using WPCordovaClassLib.Cordova;
using WPCordovaClassLib.Cordova.Commands;
using WPCordovaClassLib.Cordova.JSON;

public class PushPlugin : BaseCommand
{
    public void register(string unparsedOptions)
    {
        var options = JsonHelper.Deserialize<string[]>(unparsedOptions)[0];
        var config = JsonHelper.Deserialize<PushConfig>(options);

        Debug.WriteLine("config " + config.VariantId);
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

        [DataMember(Name="categories")]
        public IList<string> Categories { get; set; }

        [DataMember(Name = "alias")]
        public string Alias { get; set; }
    }
}