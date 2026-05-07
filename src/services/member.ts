export function getMembership() {
  return Promise.resolve({
    level: 'Lv.1 原住民',
    progress: 46,
    rights: [
      { name: '免押次数', value: '2 次' },
      { name: '配件折扣', value: '9 折' },
      { name: '光年币余额', value: '12,800' }
    ]
  })
}

export function getWallet() {
  return Promise.resolve({
    balanceCent: 128000,
    records: [
      { id: 'r1', title: '订单返佣', amountCent: 6800, type: '收入' },
      { id: 'r2', title: '提现申请', amountCent: -20000, type: '支出' }
    ]
  })
}
