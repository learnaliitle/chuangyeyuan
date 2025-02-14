<template>

  <el-container v-loading="loading" class="post-article">
    <el-header class="header">
    </el-header>
    <el-main class="main">
      <div id="editor">
        <mavon-editor
            style="height: 100%;width: 100%;"
            ref="md"
            v-model="article.mdContent"
            @imgAdd="imgAdd"
            @imgDel="imgDel">
        </mavon-editor>
      </div>

      <div style="display: flex;align-items: center;margin-top: 15px;justify-content: flex-end">
        <el-button @click="cancelEdit" v-if="from">放弃修改</el-button>
      </div>
    </el-main>
  </el-container>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { mavonEditor } from 'mavon-editor'
import 'mavon-editor/dist/css/index.css'
import {listArticle,  getArticle, uploadArticleImage} from '@/api/article/article'

// 定义 props 来接收父组件传递的 initialContent
const props = defineProps({
  initialContent: {
    type: String,
    default: ''
  }
})

const router = useRouter()
const route = useRoute()

const md = ref(null)
const saveTagInput = ref(null)
const loading = ref(false)
const categories = ref([])
const tagInputVisible = ref(false)
const tagValue = ref('')
const from = ref('')
const mdContent = ref('')
const imgUrl=ref('')
const article = reactive({
  id: '-1',
  dynamicTags: [],
  title: '',
  mdContent: props.initialContent, // 使用 props 中的 initialContent 初始化 mdContent
  cid: ''
})

// 添加 emit
const emit = defineEmits(['update:articleContent'])

// 解析 initialContent 并设置 article.mdContent
try {
  const initialData = JSON.parse(props.initialContent);
  if (initialData && typeof initialData.mdContent === 'string') {
    article.mdContent = initialData.mdContent;
    article.htmlContent=initialData.htmlContent
    console.log("initialData.htmlContent",initialData.htmlContent)
  } else {
    console.warn('initialContent 不包含有效的 mdContent');
  }
} catch (error) {
  console.error('解析 initialContent 失败:', error);
}


// 图片上传处理
const imgAdd = async (pos, $file) => {
  try {
    const formdata = new FormData();
    formdata.append('file', $file);

    const response = await uploadArticleImage(formdata);
    // 去掉前缀 /www/wwwroot/ruoyiadmin/img/
    const cleanedResponse = response.replace(/^\/www\/wwwroot\/ruoyiadmin\/img\//, '');
    const fullImageUrl = `http://118.31.112.72:8192/${cleanedResponse}`;
    console.log("response", response);
    console.log("cleanedResponse", cleanedResponse);
    console.log("fullImageUrl", fullImageUrl);

    // 保存完整的图片URL
    imgUrl.value = fullImageUrl;

    // 更新编辑器中的图片
    md.value.$img2Url(pos, fullImageUrl);

    // 手动更新 htmlContent，保留完整的图片URL
    nextTick(() => {
      if (md.value) {
        article.htmlContent = md.value.d_render;
      }
    });
  } catch (error) {
    console.error('上传图片失败:', error);
    ElMessage.error('上传图片失败!');
  }
};

// 监听初始内容
watch(() => props.initialContent, (newContent) => {
  if (newContent) {
    try {
      const initialData = JSON.parse(newContent)
      if (initialData && typeof initialData.mdContent === 'string') {
        article.mdContent = initialData.mdContent
        // 保持原有的 htmlContent，不要覆盖图片路径
        article.htmlContent = initialData.htmlContent

      }
    } catch (error) {
      console.error('解析 initialContent 失败:', error)
    }
  }
}, { immediate: true })

// 监听 mdContent 变化
watch(() => article.mdContent, (newContent) => {
  nextTick(() => {
    if (md.value) {
      // 获取新的 HTML 内容
      const newHtmlContent = md.value.d_render

      console.log("article.htmlContent",article.htmlContent)

      // 触发更新
      emit('update:articleContent', {
        mdContent: article.mdContent,
        htmlContent: article.htmlContent
      })
    }
  })
})



const getCategories = async () => {
  try {
    const resp = await listArticle({ type: 'category' })
    categories.value = resp.data
  } catch (error) {
    console.error('获取分类失败:', error)
  }
}

const loadArticle = async (id) => {
  try {
    loading.value = true
    const resp = await getArticle(id)
    if (resp.status === 200) {
      Object.assign(article, resp.data)
      article.dynamicTags = resp.data.tags.map(tag => tag.tagName)
    } else {
      ElMessage.error('页面加载失败!')
    }
  } catch (error) {
    console.error('加载文章失败:', error)
    ElMessage.error('页面加载失败!')
  } finally {
    loading.value = false
  }
}



const imgDel = (file) => {
  // 实现图片删除逻辑
}



const handleClose = (tag) => {
  article.dynamicTags.splice(article.dynamicTags.indexOf(tag), 1)
}

const showInput = () => {
  tagInputVisible.value = true
  nextTick(() => {
    saveTagInput.value?.focus()
  })
}

const cancelEdit = () => {
  router.go(-1)
}

// 在组件挂载时调用 getCategories
onMounted(() => {
  console.log("组件已挂载")
  getCategories()
  const articleId = route.params.id
  if (articleId) {
    loadArticle(articleId)
  }

  // 添加调试信息
  console.log('md ref:', md)
})
</script>

<style scoped>
.post-article > .main > #editor {
  width: 100%;
  height: 450px;
  text-align: left;
}

.post-article > .header {
  margin-top: 10px;
  padding-left: 5px;
  display: flex;
  justify-content: flex-start;
}

.post-article > .main {
  display: flex;
  flex-direction: column;
  padding-left: 5px;
  padding-top: 0px;
}

.post-article > .header > .el-tag + .el-tag {
  margin-left: 10px;
}

.post-article > .header > .button-new-tag {
  margin-left: 10px;
  height: 32px;
  line-height: 30px;
  padding-top: 0;
  padding-bottom: 0;
}

.post-article > .header > .input-new-tag {
  width: 90px;
  margin-left: 10px;
  vertical-align: bottom;
}
</style>