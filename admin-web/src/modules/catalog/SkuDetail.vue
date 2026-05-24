<template>
  <el-card v-loading="loading">
    <template #header>
      <div class="header">
        <span>{{ isNew ? '新增商品' : '编辑商品详情' }}</span>
        <el-button @click="router.back()">返回</el-button>
      </div>
    </template>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="126px">
      <el-divider content-position="left">基础信息</el-divider>
      <div class="grid">
        <el-form-item label="商品名称" prop="name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="副标题"><el-input v-model="form.subtitle" /></el-form-item>
        <el-form-item label="商品类型" prop="type">
          <el-select v-model="form.type"><el-option label="租赁" value="RENT" /><el-option label="购买" value="BUY" /><el-option label="软件" value="SOFTWARE" /></el-select>
        </el-form-item>
        <el-form-item label="品牌">
          <el-select v-model="form.brandId" filterable placeholder="选择品牌">
            <el-option v-for="brand in brands" :key="brand.id" :label="brand.name" :value="brand.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="型号 ID"><el-input-number v-model="form.modelId" :min="0" /></el-form-item>
        <el-form-item label="库存"><el-input-number v-model="form.stock" :min="0" /></el-form-item>
        <el-form-item label="原价（分）"><el-input-number v-model="form.originalPriceMinor" :min="0" /></el-form-item>
        <el-form-item label="商品标签"><el-input v-model="tagsText" placeholder="逗号分隔，例如：可租赁,支持培训" /></el-form-item>
      </div>
      <el-form-item label="简介"><el-input v-model="form.description" type="textarea" :rows="2" /></el-form-item>
      <el-form-item label="适配场景"><el-input v-model="form.adaptedScenesText" placeholder="例如：工业巡检、科研教育" /></el-form-item>
      <div class="grid">
        <el-form-item label="库存展示文案"><el-input v-model="form.stockStatusText" /></el-form-item>
        <el-form-item label="配送展示文案"><el-input v-model="form.deliveryText" /></el-form-item>
      </div>
      <el-form-item label="参数 JSON">
        <el-input v-model="form.specsJson" type="textarea" :rows="3" placeholder='{"重量":"35kg","高度":"1270mm"}' />
      </el-form-item>

      <el-divider content-position="left">商品图片与视频</el-divider>
      <div v-for="(media, index) in form.media" :key="index" class="row">
        <el-select v-model="media.type" class="compact"><el-option label="图片" value="IMAGE" /><el-option label="视频" value="VIDEO" /></el-select>
        <ImageUpload v-if="media.type === 'IMAGE'" v-model="media.url" />
        <el-input v-else v-model="media.url" placeholder="视频 URL" />
        <el-input-number v-model="media.sortOrder" :min="0" />
        <el-button type="danger" link @click="form.media.splice(index, 1)">删除</el-button>
      </div>
      <el-button @click="form.media.push({ type: 'IMAGE', url: '', sortOrder: form.media.length })">新增媒体</el-button>

      <el-divider content-position="left">价格方案</el-divider>
      <div v-for="(price, index) in form.prices" :key="index" class="row">
        <el-select v-model="price.priceType" class="wide">
          <el-option label="短期租赁" value="DAILY_RENT" /><el-option label="租赁购买" value="LEASE_BUY" /><el-option label="购买买断" value="BUY" /><el-option label="软件订阅" value="SUBSCRIPTION" />
        </el-select>
        <el-input-number v-model="price.priceMinor" :min="0" placeholder="金额（分）" />
        <el-input-number v-model="price.minDuration" :min="1" placeholder="最短时长" />
        <el-input-number v-model="price.maxDuration" :min="1" placeholder="最长时长" />
        <el-button type="danger" link @click="form.prices.splice(index, 1)">删除</el-button>
      </div>
      <el-button @click="form.prices.push({ priceType: 'DAILY_RENT', priceMinor: 0, minDuration: 1, maxDuration: 365 })">新增价格方案</el-button>

      <el-divider content-position="left">服务信息</el-divider>
      <div v-for="(service, index) in form.services" :key="index" class="row">
        <el-input v-model="service.name" placeholder="服务名称" />
        <el-input v-model="service.priceLabel" placeholder="价格文案，例如：免费" />
        <el-button type="danger" link @click="form.services.splice(index, 1)">删除</el-button>
      </div>
      <el-button @click="form.services.push({ name: '', priceLabel: '免费', sortOrder: form.services.length })">新增服务</el-button>

      <el-divider content-position="left">产品说明区块</el-divider>
      <div v-for="(section, index) in form.detailSections" :key="index" class="section-row">
        <el-input v-model="section.title" placeholder="区块标题" />
        <el-input v-model="section.content" type="textarea" :rows="2" placeholder="区块内容" />
        <el-button type="danger" link @click="form.detailSections.splice(index, 1)">删除</el-button>
      </div>
      <el-button @click="form.detailSections.push({ title: '', content: '', sortOrder: form.detailSections.length })">新增说明区块</el-button>

      <div class="actions">
        <el-button type="primary" :loading="saving" @click="handleSave">保存商品</el-button>
      </div>
    </el-form>
  </el-card>
</template>

<script setup lang="ts">
import { computed, reactive, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import ImageUpload from '@/components/ImageUpload.vue'
import http from '@/api/http'

const route = useRoute()
const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)
const saving = ref(false)
const isNew = computed(() => route.params.id === 'new')
const tagsText = ref('')
const brands = ref<Array<{ id: number; name: string }>>([])
const form = reactive({
  name: '', subtitle: '', type: 'RENT', brandId: 1, modelId: undefined as number | undefined, stock: 0,
  description: '', specsJson: '{}', originalPriceMinor: 0, adaptedScenesText: '', stockStatusText: '', deliveryText: '',
  media: [] as Array<{ type: 'IMAGE' | 'VIDEO'; url: string; sortOrder: number }>,
  prices: [] as Array<{ priceType: string; priceMinor: number; minDuration: number; maxDuration: number }>,
  services: [] as Array<{ name: string; priceLabel: string; sortOrder: number }>,
  detailSections: [] as Array<{ title: string; content: string; sortOrder: number }>,
})
const rules: FormRules = { name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }], type: [{ required: true, message: '请选择商品类型', trigger: 'change' }] }

async function fetchDetail() {
  if (isNew.value) return
  loading.value = true
  try {
    const { data } = await http.get(`/skus/${route.params.id}`)
    const detail = data.data
    Object.assign(form, detail)
    form.media = detail.media || []
    form.prices = detail.prices || []
    form.services = detail.services || []
    form.detailSections = detail.detailSections || []
    tagsText.value = (detail.tags || []).join(',')
  } finally {
    loading.value = false
  }
}

async function fetchBrands() {
  brands.value = (await http.get('/brands')).data.data || []
}

async function handleSave() {
  if (!await formRef.value?.validate().catch(() => false)) return
  try { JSON.parse(form.specsJson || '{}') } catch { ElMessage.error('参数 JSON 格式不正确'); return }
  saving.value = true
  try {
    const payload = {
      ...form,
      tags: tagsText.value.split(/[,，]/).map((item) => item.trim()).filter(Boolean),
      media: form.media.map((item, index) => ({ ...item, sortOrder: index })),
      services: form.services.map((item, index) => ({ ...item, sortOrder: index })),
      detailSections: form.detailSections.map((item, index) => ({ ...item, sortOrder: index })),
    }
    if (isNew.value) await http.post('/skus', payload); else await http.put(`/skus/${route.params.id}`, payload)
    ElMessage.success('商品已保存')
    router.push('/catalog/sku')
  } finally {
    saving.value = false
  }
}
onMounted(async () => {
  await fetchBrands()
  await fetchDetail()
})
</script>

<style scoped>
.header { display: flex; align-items: center; justify-content: space-between; }
.grid { display: grid; grid-template-columns: repeat(2, minmax(320px, 1fr)); gap: 0 20px; }
.row { display: flex; align-items: center; gap: 12px; margin-bottom: 12px; }
.row .el-input { max-width: 360px; }
.compact { width: 110px; }
.wide { width: 150px; }
.section-row { display: grid; grid-template-columns: 260px 1fr 60px; gap: 12px; align-items: start; margin-bottom: 12px; }
.actions { margin: 32px 0 10px 126px; }
</style>
