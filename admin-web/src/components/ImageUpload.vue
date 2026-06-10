<template>
  <div class="image-uploader">
    <el-upload
      :action="uploadUrl"
      :http-request="uploadRequest"
      :show-file-list="false"
      :before-upload="beforeUpload"
      :on-success="handleSuccess"
      accept="image/*"
    >
      <div v-if="modelValue" class="preview-wrapper">
        <img :src="modelValue" class="preview-img" />
        <div class="preview-mask">
          <span>更换图片</span>
        </div>
      </div>
      <div v-else class="upload-placeholder">
        <el-icon :size="28"><Plus /></el-icon>
        <span>上传图片</span>
      </div>
    </el-upload>
  </div>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import type { UploadRequestOptions } from 'element-plus'
import { uploadImage } from '@/api/upload'

const props = defineProps<{ modelValue?: string }>()
const emit = defineEmits<{ (e: 'update:modelValue', url: string): void }>()

const uploadUrl = '/api/v1/admin/upload/image'

function beforeUpload(file: File) {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('仅支持图片文件')
    return false
  }
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    ElMessage.error('图片大小不能超过 10MB')
    return false
  }
  return true
}

async function uploadRequest(options: UploadRequestOptions) {
  try {
    const response = await uploadImage(options.file)
    options.onSuccess(response)
  } catch (error) {
    options.onError(error as Parameters<UploadRequestOptions['onError']>[0])
  }
}

function handleSuccess(response: any) {
  if (response.success && response.data?.url) {
    emit('update:modelValue', response.data.url)
    ElMessage.success('上传成功')
  } else {
    ElMessage.error(response.error?.message || '上传失败')
  }
}

</script>

<style scoped>
.image-uploader { display: inline-block; }
.preview-wrapper { position: relative; width: 148px; height: 148px; border-radius: 6px; overflow: hidden; }
.preview-img { width: 100%; height: 100%; object-fit: cover; }
.preview-mask {
  position: absolute; inset: 0;
  display: flex; align-items: center; justify-content: center;
  background: rgba(0,0,0,0.4); color: #fff; opacity: 0;
  transition: opacity 0.2s;
}
.preview-wrapper:hover .preview-mask { opacity: 1; }
.upload-placeholder {
  width: 148px; height: 148px;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  border: 1px dashed #d9d9d9; border-radius: 6px; color: #8c939d;
  cursor: pointer; transition: border-color 0.2s;
}
.upload-placeholder:hover { border-color: #409eff; color: #409eff; }
</style>
