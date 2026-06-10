<template>
  <div class="video-uploader">
    <el-upload
      :http-request="uploadRequest"
      :show-file-list="false"
      :before-upload="beforeUpload"
      :on-success="handleSuccess"
      :on-error="handleError"
      accept="video/mp4,video/quicktime,video/webm,video/*"
    >
      <div v-if="modelValue" class="preview-wrapper">
        <video :src="modelValue" class="preview-video" muted />
        <div class="preview-mask">
          <span>更换视频</span>
        </div>
      </div>
      <div v-else class="upload-placeholder">
        <el-icon :size="28"><Plus /></el-icon>
        <span>上传视频</span>
      </div>
    </el-upload>
    <div class="upload-tip">支持 MP4/MOV/WebM，微信端建议使用 MP4/H.264，单个文件不超过 200MB。</div>
  </div>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import type { UploadRequestOptions } from 'element-plus'
import { uploadVideo } from '@/api/upload'

defineProps<{ modelValue?: string }>()
const emit = defineEmits<{ (e: 'update:modelValue', url: string): void }>()

function beforeUpload(file: File) {
  if (!file.type.startsWith('video/')) {
    ElMessage.error('仅支持视频文件')
    return false
  }
  const isLt200M = file.size / 1024 / 1024 < 200
  if (!isLt200M) {
    ElMessage.error('视频大小不能超过 200MB')
    return false
  }
  return true
}

async function uploadRequest(options: UploadRequestOptions) {
  try {
    const response = await uploadVideo(options.file)
    options.onSuccess(response)
  } catch (error) {
    options.onError(error as Parameters<UploadRequestOptions['onError']>[0])
  }
}

function handleSuccess(response: any) {
  if (response.success && response.data?.url) {
    emit('update:modelValue', response.data.url)
    ElMessage.success('视频上传成功')
  } else {
    ElMessage.error(response.error?.message || '视频上传失败')
  }
}

function handleError() {
  ElMessage.error('视频上传失败，请重试')
}
</script>

<style scoped>
.video-uploader { display: inline-block; }
.preview-wrapper { position: relative; width: 240px; height: 135px; border-radius: 6px; overflow: hidden; background: #111827; }
.preview-video { width: 100%; height: 100%; object-fit: cover; display: block; }
.preview-mask {
  position: absolute; inset: 0;
  display: flex; align-items: center; justify-content: center;
  background: rgba(0,0,0,0.4); color: #fff; opacity: 0;
  transition: opacity 0.2s;
}
.preview-wrapper:hover .preview-mask { opacity: 1; }
.upload-placeholder {
  width: 240px; height: 135px;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  border: 1px dashed #d9d9d9; border-radius: 6px; color: #8c939d;
  cursor: pointer; transition: border-color 0.2s;
}
.upload-placeholder:hover { border-color: #409eff; color: #409eff; }
.upload-tip { margin-top: 8px; color: #909399; font-size: 12px; line-height: 1.5; }
</style>
