export interface LatestTask {
  begin: () => number;
  isCurrent: (id: number) => boolean;
  cancel: () => void;
}

export function createLatestTask(): LatestTask {
  let latestId = 0;

  return {
    begin() {
      latestId += 1;
      return latestId;
    },
    isCurrent(id: number) {
      return id === latestId;
    },
    cancel() {
      latestId += 1;
    },
  };
}
