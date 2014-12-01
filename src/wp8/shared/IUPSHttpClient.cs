using System;
using System.Net;
using System.Threading.Tasks;

namespace AeroGear.Push
{
    public interface IUPSHttpClient
    {
        Task<HttpStatusCode> register(Installation installation);
    }
}
