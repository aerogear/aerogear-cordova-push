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

using System.Net;
using System.Threading.Tasks;

namespace AeroGear.Push
{
    /// <summary>
    ///     Http client that 'knows' how to send information to UPS (e.g. use the proper credentials)
    /// </summary>
    public interface IUPSHttpClient
    {
        /// <summary>
        ///     Register this device with the UPS
        /// </summary>
        /// <param name="installation">containing the device information</param>
        /// <returns>the http response status</returns>
        Task<HttpStatusCode> Register(Installation installation);

        /// <summary>
        ///     Send metrics to UPS e.g. this messages was used to open the app
        /// </summary>
        /// <param name="pushMetricsId">the metrics id of the message that opened the app</param>
        /// <returns></returns>
        Task<HttpStatusCode> SendMetrics(string pushMetricsId);
    }
}