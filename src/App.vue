<script setup lang="ts">
import { onLaunch } from '@dcloudio/uni-app'
import { useConfigStore } from './stores/config'
import { useSessionStore } from './stores/session'

const configStore = useConfigStore()
const sessionStore = useSessionStore()

onLaunch(() => {
  sessionStore.restore()
  configStore.bootstrap()
})
</script>

<style lang="scss">
@import './styles/tokens.scss';

page {
  min-height: 100%;
  background: $color-bg;
  color: $color-text;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
}

view,
text,
button,
input,
textarea,
scroll-view {
  box-sizing: border-box;
}

button {
  padding: 0;
  margin: 0;
  background: transparent;
  border-radius: 0;
  line-height: 1;
}

button::after {
  border: 0;
}
</style>
