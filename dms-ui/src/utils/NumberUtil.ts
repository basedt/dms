export const NumberUtil = {
  byteFormat: (size: number) => {
    if (size >= 0 && size <= 1024 * 1024) {
      return (size / 1024).toFixed(2) + "KB";
    } else if (size > 1024 * 1024 && size <= 1024 * 1024 * 1024) {
      return (size / 1024 / 1024).toFixed(2) + "MB";
    } else if (size > 1024 * 1024 * 1024 && size <= 1024 * 1024 * 1024 * 1024) {
      return (size / 1024 / 1024 / 1024).toFixed(2) + "GB";
    } else if (
      size > 1024 * 1024 * 1024 * 1024 &&
      size <= 1024 * 1024 * 1024 * 1024 * 1024
    ) {
      return (size / 1024 / 1024 / 1024 / 1024).toFixed(2) + "TB";
    }
  },
};
