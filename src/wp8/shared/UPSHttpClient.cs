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
using System.IO;
using System.Net;
using System.Runtime.Serialization.Json;
using System.Text;
using System.Threading.Tasks;

namespace AeroGear.Push
{
    /// <summary>
    ///     Implementation of the IUPSHttpClient.
    /// </summary>
    public sealed class UPSHttpClient : IUPSHttpClient
    {
        private const string AuthorizationHeader = "Authorization";
        private const string AuthorizationMethod = "Basic";
        private const string RegistrationEndpoint = "rest/registry/device";
        private const string MetricEndpoint = RegistrationEndpoint + "/pushMessage/";

        public UPSHttpClient(Uri uri, string username, string password)
        {
            Uri = uri;
            Username = username;
            Password = password;
        }

        private Uri Uri { get; set; }
        private string Username { get; set; }
        private string Password { get; set; }

        public async Task<HttpStatusCode> Register(Installation installation)
        {
            var request = CreateRequest(Uri + RegistrationEndpoint);
            request.Method = "POST";
            using (
                var postStream =
                    await
                        Task<Stream>.Factory.FromAsync(request.BeginGetRequestStream, request.EndGetRequestStream,
                            request))
            {
                var serializer = new DataContractJsonSerializer(typeof (Installation));
                serializer.WriteObject(postStream, installation);
            }

            return await ReadResponse(request);
        }

        public async Task<HttpStatusCode> SendMetrics(string pushMetricsId)
        {
            var request = CreateRequest(Uri + MetricEndpoint + pushMetricsId);
            request.Method = "PUT";

            return await ReadResponse(request);
        }

        private HttpWebRequest CreateRequest(string endpoint)
        {
            var request = (HttpWebRequest) WebRequest.Create(endpoint);
            request.ContentType = "application/json";
            request.Headers[AuthorizationHeader] = AuthorizationMethod + " " + CreateHash(Username, Password);

            return request;
        }

        private static async Task<HttpStatusCode> ReadResponse(HttpWebRequest request)
        {
            var responseObject =
                (HttpWebResponse)
                    await Task<WebResponse>.Factory.FromAsync(request.BeginGetResponse, request.EndGetResponse, request);
            await new StreamReader(responseObject.GetResponseStream()).ReadToEndAsync();
            return responseObject.StatusCode;
        }

        private static string CreateHash(string username, string password)
        {
            return Convert.ToBase64String(Encoding.UTF8.GetBytes(username + ":" + password));
        }
    }
}