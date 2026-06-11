import React from 'react';

import styles from './index.module.scss';

const Component = () => {
  return (
    <div className={styles.hideNSeek}>
      <div className={styles.statusBarNavHeader}>
        <div className={styles.statusBarIosiPhoneWi}>
          <p className={styles.time}>09:41</p>
          <img src="../image/mq7jipcy-i0lwif3.svg" className={styles.notch} />
          <div className={styles.statusIcons}>
            <img
              src="../image/mq7jipcx-b5548st.svg"
              className={styles.networkSignal}
            />
            <img src="../image/mq7jipcx-q9psklo.svg" className={styles.wiFi} />
            <img src="../image/mq7jipcx-ns002kz.svg" className={styles.battery} />
          </div>
        </div>
        <div className={styles.navigationHeaderDivi}>
          <div className={styles.navigationHeader}>
            <div className={styles.iconText}>
              <div className={styles.leftIconFrame}>
                <img src="../image/mq7jipcy-basfynt.svg" className={styles.icon} />
              </div>
              <div className={styles.autoWrapper}>
                <div className={styles.lineDivider} />
              </div>
              <div className={styles.text}>
                <p className={styles.body}>Pet Profile</p>
                <p className={styles.title}>Hide n seek</p>
              </div>
            </div>
            <div className={styles.visualText}>
              <img src="../image/mq7jipd4-k9oq63b.png" className={styles.photo} />
              <p className={styles.text2}>Bruno</p>
            </div>
          </div>
          <div className={styles.line} />
        </div>
      </div>
      <div className={styles.image}>
        <img
          src="../image/mq7jipd4-4ib1981.png"
          className={styles.imageDefaultOnboardi}
        />
      </div>
      <p className={styles.heyYouHaveGotBruno}>Hey you have got bruno!</p>
    </div>
  );
}

export default Component;
