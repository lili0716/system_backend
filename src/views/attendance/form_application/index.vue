<template>
  <h1 class="page-title">统计卡片（自定义样式）</h1>
  <ElRow :gutter="20">
    <ElCol :xs="24" :sm="12" :md="6" v-for="card in statsCards" :key="card.id" class="mb-5">
      <ArtStatsCard
        :icon="card.icon"
        :iconStyle="card.customIconStyle"
        :boxStyle="card.boxStyle"
        :title="card.title"
        :description="card.description"
        :textColor="card.textColor"
        :showArrow="card.showArrow"
      />
    </ElCol>
  </ElRow>
</template>

<script lang="ts" setup>
  /**
   * 统计卡片数据配置
   */
  const statsCards = [
    {
      id: 1,
      title: '销售产品',
      count: 1235,
      description: '鞋子、牛仔裤、派对服装、手表',
      icon: 'ri:bar-chart-box-line',
      boxStyle: '!bg-theme/10',
      customIconStyle: '!text-theme !text-3xl',
      iconStyle: 'bg-info',
      textColor: 'var(--theme-color)',
      showArrow: false
    },
    {
      id: 2,
      title: '活跃用户',
      count: 5000,
      description: '日活跃用户超过5,000+',
      icon: 'ri:account-box-2-line',
      boxStyle: '!bg-warning/10',
      customIconStyle: '!text-warning !text-3xl',
      iconStyle: 'bg-warning',
      textColor: 'var(--art-warning)',
      showArrow: false
    },
    {
      id: 3,
      title: '总收入',
      count: 35000,
      description: '月收入超过¥350,000+',
      icon: 'ri:money-cny-box-line',
      boxStyle: '!bg-secondary/10',
      customIconStyle: '!text-secondary !text-3xl',
      iconStyle: 'bg-secondary',
      textColor: 'var(--art-secondary)',
      showArrow: false
    },
    {
      id: 4,
      title: '客户评价',
      count: 4800,
      description: '平均评分4.8/5',
      icon: 'ri:message-3-line',
      boxStyle: '!bg-error/10',
      customIconStyle: '!text-error !text-3xl',
      iconStyle: 'bg-error',
      textColor: 'var(--art-error)',
      showArrow: false
    }
  ]
</script>

<style lang="sass" scoped>
  @reference '@styles/core/tailwind.css';

  .page-title {
    @apply my-5 text-xl font-medium first:mt-0;
  }
</style>
