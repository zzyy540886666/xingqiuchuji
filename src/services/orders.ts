import { orders } from '@/mock/data'

export function listOrders(status?: string) {
  return Promise.resolve(status && status !== '全部' ? orders.filter(order => order.status === status) : orders)
}

export function getOrderDetail(id: string) {
  return Promise.resolve(orders.find(order => order.id === id) || orders[0])
}

export function previewOrder(skuId: string) {
  return Promise.resolve({
    skuId,
    amountCent: 389900,
    discountCent: 70000,
    payableCent: 319900,
    payMode: 'WECHAT_PREPAY'
  })
}
