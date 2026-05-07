import { banners, homeExtraProducts, products, scenes } from '@/mock/data'

export type ProductType = 'rent' | 'buy' | 'software'

export interface ProductItem {
  id: string
  name: string
  type: ProductType
  tag: string
  priceCent: number
  originalPriceCent: number
  cover: string
  specs: string[]
  availability: string
}

const catalogProducts: ProductItem[] = [...products, ...homeExtraProducts]

export function listHomeData() {
  return Promise.resolve({ banners, scenes, products: catalogProducts })
}

export function listSkus(type?: ProductType) {
  return Promise.resolve(type ? catalogProducts.filter((item) => item.type === type) : catalogProducts)
}

export function getSkuDetail(id: string) {
  const product = catalogProducts.find((item) => item.id === id) || catalogProducts[0]
  return Promise.resolve({
    ...product,
    media: [product.cover],
    qa: [
      { question: '押金如何计算', answer: '押金与减免资格以后端试算结果为准。' },
      { question: '是否包含运费险', answer: '可用服务以后端订单预览返回为准。' },
      { question: '故障如何处理', answer: '服务中订单可进入售后流程并上传凭证。' }
    ]
  })
}

export function listSceneBundles(sceneId: string) {
  return Promise.resolve({ sceneId, products: catalogProducts })
}
