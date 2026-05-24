<template>
  <view class="page safe-bottom">
    <view class="header community-header">
      <StatusBar />
      <view class="title-bar">
        <text class="title">社区</text>
        <image class="community-dots" src="/static/icons/more.svg" mode="aspectFit" />
      </view>
      <scroll-view scroll-x show-scrollbar="false" class="category-tabs">
        <view class="tab-track">
          <text v-for="category in categories" :key="category" :class="{ active: activeCategory === category }" @tap="activeCategory = category">{{ category }}</text>
        </view>
      </scroll-view>
      <scroll-view scroll-x show-scrollbar="false" class="topic-scroll">
        <view class="topic-track">
          <view v-for="topic in topics" :key="topic.name" class="topic-pill" :class="{ selected: activeTopic === topic.name }" @tap="selectTopic(topic.name)">
            <view><text>{{ topic.name }}</text><text v-if="topic.hot" class="hot-badge">热</text></view>
            <text>{{ topic.discussions }}</text>
          </view>
        </view>
      </scroll-view>
    </view>

    <view class="feed">
      <view v-for="post in communityPosts" :key="post.id" class="post">
        <view class="post-user-row">
          <view class="user-line">
            <view class="avatar-wrap">
              <image class="avatar" :src="post.avatar" mode="aspectFill" />
              <view v-if="post.verified" class="verified"><image src="/static/icons/check.svg" mode="aspectFit" /></view>
            </view>
            <view>
              <text class="user-name">{{ post.name }}</text>
              <text class="post-time">{{ post.time }}</text>
            </view>
          </view>
          <view class="follow" :class="{ followed: followedPosts.includes(post.id) }" @tap="toggleFollow(post.id)">{{ followedPosts.includes(post.id) ? "已关注" : "+ 关注" }}</view>
        </view>
        <text class="post-content">{{ post.content }}</text>
        <text class="post-tag">#{{ post.tag }}</text>
        <view class="image-grid" :class="`cols-${Math.min(post.images.length, 3)}`">
          <image v-for="(image, index) in post.images" :key="image" class="post-image" :src="image" mode="aspectFill" @tap="previewImages(post.images, index)" />
        </view>
        <view class="post-actions">
          <view class="action-left">
            <view class="action" :class="{ liked: likedPosts.includes(post.id) }" @tap="toggleLike(post.id)"><image class="action-icon" :src="likedPosts.includes(post.id) ? '/static/icons/heart-active.svg' : '/static/icons/heart.svg'" mode="aspectFit" /><text>{{ post.likes + (likedPosts.includes(post.id) ? 1 : 0) }}</text></view>
            <view class="action" @tap="openComments(post.id)"><image class="action-icon" src="/static/icons/comment.svg" mode="aspectFit" /><text>{{ post.comments }}</text></view>
            <view class="action" :class="{ favored: favoritedPosts.includes(post.id) }" @tap="toggleFavorite(post.id)"><image class="action-icon" :src="favoritedPosts.includes(post.id) ? '/static/icons/star-active.svg' : '/static/icons/star.svg'" mode="aspectFit" /><text>{{ post.favorites + (favoritedPosts.includes(post.id) ? 1 : 0) }}</text></view>
          </view>
          <view class="action" @tap="sharePost(post.id)"><image class="action-icon" src="/static/icons/share.svg" mode="aspectFit" /><text>{{ post.shares }}</text></view>
        </view>
      </view>
    </view>

    <view class="fab" @tap="createPost"><image class="fab-icon" src="/static/icons/plus.svg" mode="aspectFit" /></view>
    <view v-if="composing" class="compose-mask" @tap="closeComposer">
      <view class="compose-card" @tap.stop>
        <view class="compose-title"><text>发布动态</text><text @tap="closeComposer">取消</text></view>
        <input v-model="draftTitle" class="compose-input" maxlength="200" placeholder="标题" />
        <textarea v-model="draftContent" class="compose-textarea" maxlength="5000" placeholder="分享你的机器人使用体验..." />
        <picker v-if="circles.length" :range="circles" range-key="name" :value="circleIndex" @change="chooseCircle">
          <view class="circle-picker">发布到：{{ circles[circleIndex]?.name }}</view>
        </picker>
        <text v-else class="circle-picker">暂无可发布圈子</text>
        <view class="compose-submit" :class="{ disabled: !canPublish || publishing }" @tap="submitPost">{{ publishing ? "发布中..." : "发布" }}</view>
      </view>
    </view>
    <BottomNav current="community" />
  </view>
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from "vue";
import BottomNav from "../../components/BottomNav.vue";
import StatusBar from "../../components/StatusBar.vue";
import { createPost as submitCommunityPost, getCircles, getFeed, type Circle } from "../../services/community";

const categories = ["推荐", "关注", "话题", "圈子", "#机器人"];
const topics = ref<{ name: string; discussions: string; hot?: boolean }[]>([]);
const communityPosts = ref<any[]>([]);
const activeCategory = ref("推荐");
const activeTopic = ref("");
const likedPosts = ref<number[]>([]);
const favoritedPosts = ref<number[]>([]);
const followedPosts = ref<number[]>([]);
const composing = ref(false);
const publishing = ref(false);
const circles = ref<Circle[]>([]);
const circleIndex = ref(0);
const draftTitle = ref("");
const draftContent = ref("");
const canPublish = computed(() => Boolean(circles.value.length && draftTitle.value.trim() && draftContent.value.trim()));

onMounted(async () => {
  try {
    const feedData = await getFeed({ limit: 20 });
    communityPosts.value = (feedData || []).map((p: any) => ({
      id: p.id,
      avatar: p.authorAvatar || '/static/images/avatar.png',
      name: p.authorName || p.authorNickname || '匿名用户',
      time: p.createdAt || '',
      verified: false,
      content: p.content || '',
      tag: p.topicName || `话题 ${p.circleId || ''}`,
      images: p.images || (p.mediaList || []).map((item: any) => item.url),
      likes: p.likeCount || 0,
      comments: p.commentCount || 0,
      favorites: 0,
      shares: 0,
    }));
  } catch {
    communityPosts.value = [];
  }
  try {
    circles.value = await getCircles();
    topics.value = circles.value.map((circle: any) => ({ name: `# ${circle.name}`, discussions: `${circle.postCount || 0}讨论`, hot: (circle.postCount || 0) > 5 }));
  } catch {
    topics.value = [];
  }
});

function toggleLike(id: number) {
  likedPosts.value = likedPosts.value.includes(id) ? likedPosts.value.filter((item) => item !== id) : [...likedPosts.value, id];
}

function toggleFavorite(id: number) {
  favoritedPosts.value = favoritedPosts.value.includes(id) ? favoritedPosts.value.filter((item) => item !== id) : [...favoritedPosts.value, id];
}

function toggleFollow(id: number) {
  followedPosts.value = followedPosts.value.includes(id) ? followedPosts.value.filter((item) => item !== id) : [...followedPosts.value, id];
}

function selectTopic(name: string) {
  activeTopic.value = activeTopic.value === name ? "" : name;
  uni.showToast({ title: activeTopic.value ? `已选择 ${name}` : "已取消话题筛选", icon: "none" });
}

function previewImages(images: string[], current: number) {
  uni.previewImage({ urls: images, current: images[current] });
}

function openComments(_id: number) {
  uni.showToast({ title: "评论区将在帖子详情中展示", icon: "none" });
}

function sharePost(id: number) {
  uni.setClipboardData({
    data: `/pages/community/index?postId=${id}`,
    success: () => uni.showToast({ title: "动态链接已复制", icon: "none" }),
  });
}

async function createPost() {
  composing.value = true;
  if (circles.value.length) return;
  try {
    circles.value = await getCircles();
  } catch (error: any) {
    uni.showToast({ title: error.message || "圈子加载失败", icon: "none" });
  }
}

function chooseCircle(event: { detail: { value: string } }) {
  circleIndex.value = Number(event.detail.value);
}

function closeComposer() {
  if (!publishing.value) composing.value = false;
}

async function submitPost() {
  if (!canPublish.value || publishing.value) return;
  publishing.value = true;
  try {
    await submitCommunityPost({
      circleId: circles.value[circleIndex.value].id,
      title: draftTitle.value.trim(),
      content: draftContent.value.trim(),
    });
    composing.value = false;
    draftTitle.value = "";
    draftContent.value = "";
    uni.showToast({ title: "已提交审核", icon: "none" });
  } catch (error: any) {
    uni.showToast({ title: error.message || "发布失败", icon: "none" });
  } finally {
    publishing.value = false;
  }
}
</script>

<style scoped lang="scss">
.community-header {
  border-bottom: 1rpx solid #f3f4f6;
}

.community-dots {
  width: 44rpx;
  height: 44rpx;
  display: block;
}

.category-tabs {
  border-bottom: 1rpx solid #f3f4f6;
}

.tab-track,
.topic-track {
  display: flex;
  gap: 16rpx;
  padding: 18rpx 28rpx;
}

.tab-track text {
  height: 50rpx;
  padding: 0 26rpx;
  border-radius: 999rpx;
  color: #4b5563;
  font-size: 24rpx;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}

.tab-track .active {
  background: #0a4bfe;
  color: #fff;
}

.topic-scroll {
  background: #fff;
}

.topic-pill {
  flex-shrink: 0;
  min-width: 210rpx;
  background: #f5f6f8;
  border-radius: 999rpx;
  padding: 14rpx 24rpx;
}

.topic-pill.selected {
  background: #e8f1ff;
}

.topic-pill view {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.topic-pill view text:first-child {
  font-size: 24rpx;
  color: #111827;
}

.topic-pill > text {
  display: block;
  margin-top: 4rpx;
  color: #9ca3af;
  font-size: 20rpx;
}

.hot-badge {
  background: #0a4bfe;
  color: #fff !important;
  border-radius: 999rpx;
  padding: 1rpx 10rpx;
  font-size: 16rpx !important;
  font-weight: 900;
}

.feed {
  margin-top: 16rpx;
}

.post {
  background: #fff;
  margin-bottom: 16rpx;
  padding: 26rpx 28rpx 22rpx;
}

.post-user-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20rpx;
}

.user-line {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.avatar-wrap {
  position: relative;
  width: 72rpx;
  height: 72rpx;
}

.avatar {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  background: #e5e7eb;
}

.verified {
  position: absolute;
  right: -2rpx;
  bottom: -2rpx;
  width: 24rpx;
  height: 24rpx;
  border-radius: 50%;
  background: #0a4bfe;
  border: 4rpx solid #fff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.verified image {
  width: 14rpx;
  height: 14rpx;
  display: block;
}

.user-name,
.post-time {
  display: block;
}

.user-name {
  font-size: 26rpx;
  font-weight: 900;
  color: #111827;
}

.post-time {
  margin-top: 4rpx;
  color: #9ca3af;
  font-size: 22rpx;
}

.follow {
  height: 48rpx;
  padding: 0 26rpx;
  border-radius: 999rpx;
  background: #0a4bfe;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22rpx;
  font-weight: 700;
}

.follow.followed {
  background: #eef2ff;
  color: #0a4bfe;
}

.post-content {
  white-space: pre-line;
  display: block;
  color: #111827;
  font-size: 26rpx;
  line-height: 1.6;
  margin-bottom: 16rpx;
}

.post-tag {
  display: inline-flex;
  background: #fff3e0;
  color: #ff9800;
  font-size: 20rpx;
  font-weight: 700;
  padding: 4rpx 14rpx;
  border-radius: 6rpx;
  margin-bottom: 18rpx;
}

.image-grid {
  display: grid;
  gap: 12rpx;
  margin-bottom: 18rpx;
}

.cols-2 { grid-template-columns: repeat(2, 1fr); }
.cols-3 { grid-template-columns: repeat(3, 1fr); }

.post-image {
  width: 100%;
  height: 210rpx;
  border-radius: 16rpx;
  background: #f3f4f6;
}

.cols-3 .post-image {
  height: 210rpx;
}

.post-actions {
  border-top: 1rpx solid #f9fafb;
  padding-top: 18rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.action-left {
  display: flex;
  gap: 44rpx;
}

.action {
  display: flex;
  align-items: center;
  gap: 8rpx;
  color: #6b7280;
  font-size: 22rpx;
}

.liked {
  color: #f53f3f;
}

.favored {
  color: #ff9800;
}

.action-icon {
  width: 30rpx;
  height: 30rpx;
  display: block;
}

.fab {
  position: fixed;
  right: 28rpx;
  bottom: calc(132rpx + env(safe-area-inset-bottom));
  width: 104rpx;
  height: 104rpx;
  border-radius: 50%;
  background: #0a4bfe;
  box-shadow: 0 12rpx 28rpx rgba(10, 75, 254, 0.35);
  z-index: 35;
  display: flex;
  align-items: center;
  justify-content: center;
}

.fab-icon {
  width: 46rpx;
  height: 46rpx;
  display: block;
}

.compose-mask {
  position: fixed;
  inset: 0;
  z-index: 50;
  display: flex;
  align-items: flex-end;
  background: rgba(17, 24, 39, 0.5);
}

.compose-card {
  width: 100%;
  padding: 30rpx 28rpx calc(30rpx + env(safe-area-inset-bottom));
  border-radius: 32rpx 32rpx 0 0;
  background: #fff;
}

.compose-title {
  display: flex;
  justify-content: space-between;
  margin-bottom: 24rpx;
  font-size: 30rpx;
  font-weight: 700;
}

.compose-title text:last-child {
  color: #6b7280;
  font-weight: 400;
}

.compose-input,
.compose-textarea {
  box-sizing: border-box;
  width: 100%;
  padding: 18rpx 22rpx;
  border-radius: 16rpx;
  background: #f5f6f8;
  font-size: 27rpx;
}

.compose-textarea {
  height: 210rpx;
  margin-top: 16rpx;
}

.circle-picker {
  display: block;
  margin: 20rpx 0;
  color: #6b7280;
  font-size: 25rpx;
}

.compose-submit {
  height: 80rpx;
  border-radius: 999rpx;
  background: #0a4bfe;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  font-weight: 700;
}

.compose-submit.disabled {
  opacity: 0.45;
}
</style>
