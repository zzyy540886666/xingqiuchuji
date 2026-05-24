import React, { useState } from 'react';
import {
  MoreHorizontal,
  Heart,
  MessageCircle,
  Star,
  Share2,
  Plus,
  Battery,
  Wifi,
  Signal,
} from 'lucide-react';
import { ImageWithFallback } from '../components/figma/ImageWithFallback';

const CATEGORIES = ['推荐', '关注', '话题', '圈子', '#机器人'];

const TRENDING_TOPICS = [
  { id: 1, name: '# 展会机器人', discussions: '1287讨论', isHot: true },
  { id: 2, name: '# 门店引流', discussions: '982讨论', isHot: true },
  { id: 3, name: '# 年会互动', discussions: '562讨论', isHot: false },
  { id: 4, name: '# AI 分身', discussions: '421讨论', isHot: false },
  { id: 5, name: '# 例行巡检', discussions: '298讨论', isHot: false },
];

const POSTS = [
  {
    id: 1,
    user: {
      avatar: 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100',
      name: '宇树科技Unitree',
      time: '3小时前',
      isVerified: true,
    },
    content: 'Go2 Pro 新性能提升！\n感谢大家对我们的支持，经过团队的努力，我们对 Go2 Pro 进行了全面升级，性能提升 30%，续航增加 1 小时！',
    tag: '经验',
    images: [
      'https://images.unsplash.com/photo-1559715541-d4fc97b8d6dd?w=400',
      'https://images.unsplash.com/photo-1485827404703-89b55fcc595e?w=400',
      'https://images.unsplash.com/photo-1563207153-f403bf289096?w=400',
    ],
    likes: 188,
    comments: 45,
    favorites: 62,
    shares: 12,
  },
  {
    id: 2,
    user: {
      avatar: 'https://images.unsplash.com/photo-1599566150163-29194dcaad36?w=100',
      name: 'RoboX工作室',
      time: '5小时前',
      isVerified: false,
    },
    content: '分享一下我们团队用 Go2 四足机器人进行的舞蹈编排！整个项目历时一个月的时间调整，从动作设计到程序调试每一步都充满了挑战和乐趣。',
    tag: '分享',
    images: [
      'https://images.unsplash.com/photo-1581091226825-a6a2a5aee158?w=400',
      'https://images.unsplash.com/photo-1485827404703-89b55fcc595e?w=400',
      'https://images.unsplash.com/photo-1563207153-f403bf289096?w=400',
    ],
    likes: 256,
    comments: 78,
    favorites: 134,
    shares: 23,
  },
  {
    id: 3,
    user: {
      avatar: 'https://images.unsplash.com/photo-1527980965255-d3b416303d12?w=100',
      name: 'AI研发君',
      time: '8小时前',
      isVerified: false,
    },
    content: '想问 下，机器狗在复杂地形下的稳定性怎么样？我们项目需要在山地环境使用，有经验的朋友可以分享一下体验吗？',
    tag: '求助',
    images: [
      'https://images.unsplash.com/photo-1518770660439-4636190af475?w=400',
      'https://images.unsplash.com/photo-1485827404703-89b55fcc595e?w=400',
    ],
    likes: 89,
    comments: 42,
    favorites: 28,
    shares: 5,
  },
];

export default function Community() {
  const [activeCategory, setActiveCategory] = useState('推荐');
  const [likedPosts, setLikedPosts] = useState<number[]>([]);
  const [favoritedPosts, setFavoritedPosts] = useState<number[]>([]);

  const toggleLike = (postId: number) => {
    setLikedPosts((prev) =>
      prev.includes(postId) ? prev.filter((id) => id !== postId) : [...prev, postId]
    );
  };

  const toggleFavorite = (postId: number) => {
    setFavoritedPosts((prev) =>
      prev.includes(postId) ? prev.filter((id) => id !== postId) : [...prev, postId]
    );
  };

  return (
    <div className="max-w-[480px] mx-auto bg-[#F5F6F8] min-h-screen relative font-sans overflow-x-hidden text-gray-900 shadow-xl pb-[90px]">
      {/* Sticky Header Section */}
      <div className="sticky top-0 z-50 bg-white">
        {/* Fake iOS Status Bar */}
        <div className="flex justify-between items-center px-6 pt-3 pb-1 text-black font-semibold text-[15px]">
          <span>9:41</span>
          <div className="flex items-center gap-1.5">
            <Signal className="w-[18px] h-[18px]" />
            <Wifi className="w-4 h-4" />
            <Battery className="w-6 h-6" />
          </div>
        </div>

        {/* Title Bar */}
        <div className="flex items-center justify-between px-4 py-2">
          <h1 className="text-[18px] font-bold text-gray-900">社区</h1>
          <MoreHorizontal className="w-[22px] h-[22px] text-gray-900 cursor-pointer" strokeWidth={2} />
        </div>

        {/* Category Tabs */}
        <div className="flex items-center gap-2 px-4 py-2.5 overflow-x-auto [&::-webkit-scrollbar]:hidden [-ms-overflow-style:none] [scrollbar-width:none] border-b border-gray-100">
          {CATEGORIES.map((category) => (
            <button
              key={category}
              onClick={() => setActiveCategory(category)}
              className={`px-3.5 py-1 rounded-full text-[12px] font-medium whitespace-nowrap transition-colors ${
                activeCategory === category
                  ? 'bg-[#0A4BFE] text-white shadow-sm'
                  : 'bg-transparent text-gray-600 hover:bg-gray-100'
              }`}
            >
              {category}
            </button>
          ))}
        </div>

        {/* Trending Topics */}
        <div className="py-3 bg-white border-b border-gray-100">
          <div className="flex items-center gap-2.5 px-4 overflow-x-auto [&::-webkit-scrollbar]:hidden [-ms-overflow-style:none] [scrollbar-width:none] scroll-smooth">
            {TRENDING_TOPICS.map((topic) => (
              <div
                key={topic.id}
                className="flex flex-col items-start cursor-pointer group shrink-0 bg-[#F5F6F8] hover:bg-gray-200 transition-colors rounded-full px-3.5 py-2"
              >
                <div className="flex items-center gap-1 mb-0.5">
                  <span className="text-[12px] font-medium text-gray-900 group-hover:text-[#0A4BFE] transition-colors whitespace-nowrap">
                    {topic.name}
                  </span>
                  {topic.isHot && (
                    <div className="bg-[#0A4BFE] text-white text-[8px] px-1.5 py-0.5 rounded-full font-bold">
                      热
                    </div>
                  )}
                </div>
                <span className="text-[10px] text-gray-400 whitespace-nowrap">{topic.discussions}</span>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Posts Feed */}
      <div className="mt-2">
        {POSTS.map((post) => (
          <div key={post.id} className="bg-white mb-2 px-4 py-3.5">
            {/* User Info */}
            <div className="flex items-center justify-between mb-2.5">
              <div className="flex items-center gap-2">
                <div className="relative">
                  <div className="w-9 h-9 rounded-full overflow-hidden bg-gray-200">
                    <ImageWithFallback
                      src={post.user.avatar}
                      className="w-full h-full object-cover"
                      alt={post.user.name}
                    />
                  </div>
                  {post.user.isVerified && (
                    <div className="absolute -bottom-0.5 -right-0.5 w-3.5 h-3.5 bg-[#0A4BFE] rounded-full flex items-center justify-center border-2 border-white">
                      <svg className="w-2 h-2 text-white" viewBox="0 0 12 12" fill="none">
                        <path
                          d="M2 6L5 9L10 3"
                          stroke="currentColor"
                          strokeWidth="2.5"
                          strokeLinecap="round"
                          strokeLinejoin="round"
                        />
                      </svg>
                    </div>
                  )}
                </div>
                <div>
                  <div className="flex items-center gap-1">
                    <span className="text-[13px] font-bold text-gray-900">{post.user.name}</span>
                  </div>
                  <span className="text-[11px] text-gray-400">{post.user.time}</span>
                </div>
              </div>
              <button className="bg-[#0A4BFE] hover:bg-blue-700 transition-colors text-white text-[11px] px-3.5 py-1 rounded-full font-medium">
                + 关注
              </button>
            </div>

            {/* Post Content */}
            <div className="mb-2.5">
              <p className="text-[13px] text-gray-900 leading-[1.6] whitespace-pre-line mb-2">
                {post.content}
              </p>
              <span className="inline-block bg-[#FFF3E0] text-[#FF9800] text-[10px] px-2 py-0.5 rounded font-medium">
                #{post.tag}
              </span>
            </div>

            {/* Post Images */}
            {post.images && post.images.length > 0 && (
              <div className={`grid gap-2 mb-2.5 ${
                post.images.length === 1
                  ? 'grid-cols-1'
                  : post.images.length === 2
                  ? 'grid-cols-2'
                  : 'grid-cols-3'
              }`}>
                {post.images.map((image, index) => (
                  <div
                    key={index}
                    className={`rounded-lg overflow-hidden bg-gray-100 cursor-pointer ${
                      post.images.length === 1
                        ? 'aspect-[16/10]'
                        : post.images.length === 2
                        ? 'aspect-[3/2]'
                        : 'aspect-square'
                    }`}
                  >
                    <ImageWithFallback
                      src={image}
                      className="w-full h-full object-cover hover:scale-105 transition-transform duration-300"
                      alt={`Post image ${index + 1}`}
                    />
                  </div>
                ))}
              </div>
            )}

            {/* Action Buttons */}
            <div className="flex items-center justify-between pt-2.5 border-t border-gray-50">
              <div className="flex items-center gap-6">
                <button
                  onClick={() => toggleLike(post.id)}
                  className="flex items-center gap-1 cursor-pointer group"
                >
                  <Heart
                    className={`w-[17px] h-[17px] transition-colors ${
                      likedPosts.includes(post.id)
                        ? 'fill-[#F53F3F] text-[#F53F3F]'
                        : 'text-gray-400 group-hover:text-gray-600'
                    }`}
                    strokeWidth={2}
                  />
                  <span className={`text-[11px] ${
                    likedPosts.includes(post.id) ? 'text-[#F53F3F] font-medium' : 'text-gray-500'
                  }`}>
                    {post.likes + (likedPosts.includes(post.id) ? 1 : 0)}
                  </span>
                </button>
                <button className="flex items-center gap-1 cursor-pointer group">
                  <MessageCircle
                    className="w-[17px] h-[17px] text-gray-400 group-hover:text-gray-600 transition-colors"
                    strokeWidth={2}
                  />
                  <span className="text-[11px] text-gray-500">{post.comments}</span>
                </button>
                <button
                  onClick={() => toggleFavorite(post.id)}
                  className="flex items-center gap-1 cursor-pointer group"
                >
                  <Star
                    className={`w-[17px] h-[17px] transition-colors ${
                      favoritedPosts.includes(post.id)
                        ? 'fill-[#FF9800] text-[#FF9800]'
                        : 'text-gray-400 group-hover:text-gray-600'
                    }`}
                    strokeWidth={2}
                  />
                  <span className={`text-[11px] ${
                    favoritedPosts.includes(post.id) ? 'text-[#FF9800] font-medium' : 'text-gray-500'
                  }`}>
                    {post.favorites + (favoritedPosts.includes(post.id) ? 1 : 0)}
                  </span>
                </button>
              </div>
              <button className="flex items-center gap-1 cursor-pointer group">
                <Share2
                  className="w-[17px] h-[17px] text-gray-400 group-hover:text-gray-600 transition-colors"
                  strokeWidth={2}
                />
                <span className="text-[11px] text-gray-500">{post.shares}</span>
              </button>
            </div>
          </div>
        ))}
      </div>

      {/* Floating Action Button */}
      <button className="fixed bottom-[110px] right-4 w-[52px] h-[52px] bg-[#0A4BFE] rounded-full shadow-lg shadow-blue-500/40 flex items-center justify-center cursor-pointer hover:bg-blue-700 transition-colors z-40">
        <Plus className="w-6 h-6 text-white" strokeWidth={3} />
      </button>
    </div>
  );
}
