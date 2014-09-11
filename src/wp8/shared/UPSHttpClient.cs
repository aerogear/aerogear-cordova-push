using System;
using System.Diagnostics;
using System.IO;
using System.Net;
using System.Runtime.Serialization.Json;
using System.Text;
using System.Threading.Tasks;

namespace AeroGear.Push
{
    public sealed class UPSHttpClient : IUPSHttpClient
    {
        private const string AUTHORIZATION_HEADER = "Authorization";
        private const string AUTHORIZATION_METHOD = "Basic";
        private const string REGISTRATION_ENDPOINT = "rest/registry/device";

        private HttpWebRequest request;

        public UPSHttpClient(Uri uri, string username, string password)
        {
            request = (HttpWebRequest)WebRequest.Create(uri.ToString() + REGISTRATION_ENDPOINT);
            request.ContentType = "application/json";
            request.Headers[AUTHORIZATION_HEADER] = AUTHORIZATION_METHOD + " " + CreateHash(username, password);
            request.Method = "POST";
        }

        public async Task<HttpStatusCode> register(Installation installation)
        {
            using (var postStream = await Task<Stream>.Factory.FromAsync(request.BeginGetRequestStream, request.EndGetRequestStream, request))
            {
                DataContractJsonSerializer serializer = new DataContractJsonSerializer(typeof(Installation));
                serializer.WriteObject(postStream, installation);
            }

            HttpWebResponse responseObject = (HttpWebResponse) await Task<WebResponse>.Factory.FromAsync(request.BeginGetResponse, request.EndGetResponse, request);
            var responseStream = responseObject.GetResponseStream();
            var streamReader = new StreamReader(responseStream);

            await streamReader.ReadToEndAsync();
            return responseObject.StatusCode;
        }

        private static string CreateHash(string username, string password)
        {
            return Convert.ToBase64String(UTF8Encoding.UTF8.GetBytes(username + ":" + password));
        }
    }
}