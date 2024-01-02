<template>
  <div>
    <StringType v-if="keyItem.result == 'string'" :keyItem="keyItem" :serviceID="serviceID" :selectDB="selectDB"
      @StringSave="StringSave" @handleDel="handleDel" />
    <HashType v-if="keyItem.result == 'hash'" :keyItem="keyItem" :serviceID="serviceID" :selectDB="selectDB"
      @handleDel="handleDel" />
    <ListType v-if="keyItem.result == 'list'" :keyItem="keyItem" :serviceID="serviceID" :selectDB="selectDB"
      @handleDel="handleDel" />
    <SetType v-if="keyItem.result == 'set'" :keyItem="keyItem" :serviceID="serviceID" :selectDB="selectDB"
      @handleDel="handleDel" />
    <ZsetType v-if="keyItem.result == 'zset'" :keyItem="keyItem" :serviceID="serviceID" :selectDB="selectDB"
      @handleDel="handleDel" />
    <Stream v-if="keyItem.result == 'stream'" :keyItem="keyItem" :serviceID="serviceID" :selectDB="selectDB" 
      @handleDel="handleDel"
    />
  </div>
</template>
<script >
import StringType from './StringType.vue'
import HashType from './HashType.vue'
import ListType from './ListType.vue'
import SetType from './SetType.vue'
import ZsetType from './ZsetType.vue'
import Stream from './Stream.vue'
export default {
  components: {
    StringType,
    HashType,
    ListType,
    SetType,
    ZsetType,
    Stream
  },
  props: {
    keyItem: {
      type: Object
    },
    selectDB: {
      type: Number
    },
    serviceID: {
      type: Number
    }
  },
  data() {
    return {

    }
  },
  methods: {
    StringSave(data) {
      this.$emit('StringSave', data)
    },
    handleDel(data) {
      this.$emit('handleDel', data)
    }
  }

}

</script>

<style lang="scss" scoped>

</style>