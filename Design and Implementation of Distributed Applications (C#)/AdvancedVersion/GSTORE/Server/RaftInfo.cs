using System;
using GStoreLib;
using System.Timers;
using System.Collections.Generic;

namespace ServerGStore {
    public class RaftInfo {

        enum RaftState {
            MASTER,
            FOLLOWER,
            CANDIDATE
        }

        public static readonly int MIN_DELAY = 150;
        public static readonly int MAX_DELAY = 300;
        public static readonly int TIMEOUT = (MIN_DELAY + MAX_DELAY) / 2;

        private int term;
        private int votedTerm;
        private RaftState state;

        public int Term { get { lock (this) return term; } set { lock (this) term = value; } }
        public List<KeyValuePair<string, string>> Logs { get; set; }
        public PartitionInfo PInfo { get; set; }

        public RaftInfo(PartitionInfo pInfo, bool isMaster) {
            term = 0;
            votedTerm = 0;
            state = isMaster ? RaftState.MASTER : RaftState.FOLLOWER;
            Logs = new List<KeyValuePair<string, string>>();
            PInfo = pInfo;
        }

        public List<KeyValuePair<string, string>> GetLogsRange(int index, int count) {

            lock (this) {
                if (Logs.Count - index >= count) return Logs.GetRange(index, count);
                else return new List<KeyValuePair<string, string>>();
            }
        }

        public int AddLog(string idObj, string val) {
            lock (this) {
                Logs.Add(new KeyValuePair<string, string>(idObj, val));
                return PInfo.IncrTag();
            }
        }

        public int RemoveLog(int index) {
            lock (this) {
                Logs.RemoveAt(index);
                return PInfo.DecrTag();
            }
        }

        public void AddAllLogs(List<KeyValuePair<string, string>> logs) {
            Logs.AddRange(logs);
            PInfo.Tag = Logs.Count;
        }

        public void RemoveAllLogs(int newTag) {
            Logs.RemoveRange(newTag, Logs.Count - newTag);
            PInfo.Tag = newTag;
        }

        public bool IsMaster() {
            lock (this) return state == RaftState.MASTER;
        }

        public bool IsFollower() {
            lock (this) return state == RaftState.FOLLOWER;
        }

        public bool IsCandidate() {
            lock (this) return state == RaftState.CANDIDATE;
        }

        public void BecomeMaster(Timer aTimer) {
            lock (this) {
                state = RaftState.MASTER;
                ResetTimeout(aTimer, TIMEOUT);
            }
        }

        public void BecomeFollower(Timer aTimer) {
            lock (this) {
                state = RaftState.FOLLOWER;
                ResetTimeout(aTimer, TIMEOUT + GetRandomDelay(1));
            }
        }

        public void BecomeCandidate(Timer aTimer) {
            lock (this) {
                state = RaftState.CANDIDATE;
                term++;
                StopTimeout(aTimer);
            }
        }

        public void CandidateTimeout(Timer aTimer) {
            lock (this) {
                if (state == RaftState.CANDIDATE) {
                    ResetTimeout(aTimer, GetRandomDelay(2));
                }
            }
        }

        public static int GetRandomDelay(int factor) {
            return new Random().Next(MIN_DELAY, factor * MAX_DELAY);
        }

        public void ResetTimeout(Timer aTimer, int newInterval) {
            StopTimeout(aTimer);
            lock (aTimer) {
                aTimer.Interval = newInterval;
                aTimer.Start();
            }
        }

        public void StopTimeout(Timer aTimer) {
            lock (aTimer) if (aTimer.Enabled) aTimer.Stop();
        }

        public bool GetVote(int term) {
            lock (this) {
                if (term > votedTerm) {
                    votedTerm = term;
                    return true;
                }
            }
            return false;
        }
    }
}
