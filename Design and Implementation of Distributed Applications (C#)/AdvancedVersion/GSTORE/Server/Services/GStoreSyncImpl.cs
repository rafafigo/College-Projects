using Grpc.Core;
using GStoreLib;
using System;
using System.Threading.Tasks;
using System.Collections.Generic;

namespace ServerGStore {
    class GStoreSyncImpl : GStoreSync.GStoreSyncBase {

        private readonly ServerDomain store;
        private readonly ServerManager man;
        private readonly int minDelay;
        private readonly int maxDelay;

        public GStoreSyncImpl(ServerDomain store, ServerManager man, int minDelay, int maxDelay) {
            this.store = store;
            this.man = man;
            this.minDelay = minDelay;
            this.maxDelay = maxDelay;
        }

        public override Task<AppendEntriesReply> AppendEntries(AppendEntriesRequest req, ServerCallContext _) {
            man.CheckFreeze();
            var sInfo = new ServerInfo(req.Mid, req.Tag, req.Term);
            var entries = new List<KeyValuePair<string, string>>();
            foreach (var e in req.Entries) entries.Add(new KeyValuePair<string, string>(e.IdObj, e.Val));

            var mySInfo = man.RcvAppendEntries(req.IdPart, sInfo, entries);
            if (mySInfo.Term <= sInfo.Term) foreach (var e in entries) store.Write(req.IdPart, e.Key, e.Value);

            var res = new AppendEntriesReply {
                Term = mySInfo.Term,
                Tag = mySInfo.Tag,
                Mid = mySInfo.Mid
            };
            Lib.Sleep(new Random().Next(minDelay, maxDelay));
            return Task.FromResult(res);
        }

        public override Task<RequestVoteReply> RequestVote(RequestVoteRequest req, ServerCallContext _) {
            man.CheckFreeze();
            var res = new RequestVoteReply { Accept = man.RcvRequestVote(req.IdPart, req.Term, req.Tag) };
            Lib.Sleep(new Random().Next(minDelay, maxDelay));
            return Task.FromResult(res);
        }
    }
}
