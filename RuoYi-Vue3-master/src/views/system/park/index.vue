<template>
  <div class="app-container">
    <el-row :gutter="20">
      <!--用户数据-->
      <el-col :span="24">
        <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">

        </el-form>

        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button
                type="primary"
                plain
                icon="Plus"
                @click="handleAdd"
                v-hasPermi="['system:user:add']"
            >新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button
                type="danger"
                plain
                icon="Delete"
                :disabled="multiple"
                @click="handleDeleteList"
                v-hasPermi="['system:user:remove']"
            >删除</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button
                type="warning"
                plain
                icon="Download"
                @click="handleExport"
                v-hasPermi="['system:user:export']"
            >导出</el-button>
          </el-col>
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns"></right-toolbar>
        </el-row>

        <el-table v-loading="loading" :data="parkList" @selection-change="handleSelectionChange" class="full-width-table">
          <el-table-column type="selection" width="50" align="center" />
          <el-table-column label="序号" align="center" key="parkId" prop="parkId" v-if="columns[0].visible">
            <template #default="scope">
              {{scope.$index+1}}
            </template>
          </el-table-column>
          <el-table-column label="创业园名称" align="center" key="parkName" prop="parkName" v-if="columns[1].visible" :show-overflow-tooltip="true" />
          <el-table-column label="所属行业" align="center" key="industry" prop="industry" v-if="columns[2].visible" :show-overflow-tooltip="true" />
          <el-table-column label="入驻地址" align="center" key="address" prop="address" v-if="columns[3].visible" :show-overflow-tooltip="true" />
          <el-table-column label="团队成员数量" align="center" key="companyMembers" prop="companyMembers" v-if="columns[4].visible" width="120" />
          <el-table-column label="创业毕业生数量" align="center" key="graduateCount" prop="graduateCount" v-if="columns[4].visible" width="120" />
          <el-table-column label="创业项目数量" align="center" key="projectCount" prop="projectCount" v-if="columns[4].visible" width="120" />
          <el-table-column label="培训学员数量" align="center" key="traineeCount" prop="traineeCount" v-if="columns[4].visible" width="120" />
          <el-table-column label="带动就业人数数量" align="center" key="employmentCount" prop="employmentCount" v-if="columns[4].visible" width="120" />
          <el-table-column label="政府补贴金额(万)" align="center" key="governmentSubsidy" prop="governmentSubsidy" v-if="columns[4].visible" width="120" />
          <el-table-column label="入驻日期" align="center" key="entryDate" prop="entryDate" v-if="columns[1].visible" width="160">
            <template #default="scope">
              <span>{{ parseTime(scope.row.entryDate) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="资金支持金额(万)" align="center" key="financialSupport" prop="financialSupport" v-if="columns[1].visible" width="120" />
          <el-table-column label="投入经费(万)" align="center" key="investmentAmount" prop="investmentAmount" v-if="columns[1].visible" width="120" />
          <el-table-column label="整体面积" align="center" key="totalArea" prop="totalArea" v-if="columns[1].visible" width="120" />
          <el-table-column label="创建时间" align="center" prop="createdAt" v-if="columns[1].visible" width="160">
            <template #default="scope">
              <span>{{ parseTime(scope.row.createdAt) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="更新时间" align="center" prop="updatedAt" v-if="columns[1].visible" width="160">
            <template #default="scope">
              <span>{{ parseTime(scope.row.updatedAt) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" width="150" class-name="small-padding fixed-width">
            <template #default="scope">
              <el-tooltip content="修改" placement="top" >
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['system:industry:edit']"></el-button>
              </el-tooltip>
              <el-tooltip content="删除" placement="top" >
                <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['system:industry:edit']"></el-button>
              </el-tooltip>
            </template>
          </el-table-column>
        </el-table>

        <pagination
            v-show="total > 0"
            :total="total"
            v-model:page="queryParams.pageNum"
            v-model:limit="queryParams.pageSize"
            @pagination="getList"
        />
      </el-col>
    </el-row>

    <!-- 添加或修改用户配置对话框 -->
    <el-dialog :title="title" v-model="open" width="750px" append-to-body>
      <el-form :model="form" :rules="rules" ref="parkRef" label-width="80px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="名称" prop="parkName">
              <el-input v-model="form.parkName" placeholder="请输入创业园名称" maxlength="22" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属行业" prop="industry">
              <el-select v-model="form.industry" placeholder="请选择行业">
                <el-option
                    v-for="option in form.industryOptions"
                    :key="option"
                    :label="option"
                    :value="option"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="入驻地址" prop="address">
              <el-input v-model="form.address" placeholder="请输入入驻地址" maxlength="100" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="成员数量" prop="companyMembers">
              <el-input v-model.number="form.companyMembers" placeholder="请输入团队成员数量" type="number" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="毕业生数量" prop="graduateCount" class="no-wrap-label">
              <el-input v-model="form.graduateCount" placeholder="请输入创业毕业生数量" maxlength="100" type="number" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="项目数量" prop="projectCount" class="no-wrap-label">
              <el-input v-model.number="form.projectCount" placeholder="请输入创业项目数量" type="number" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="学员数量" prop="traineeCount" class="no-wrap-label">
              <el-input v-model="form.traineeCount" placeholder="请输入培训学员数量" maxlength="100" type="number" />
            </el-form-item>
          </el-col>
          <el-col :span="12" >
            <el-form-item label="带动就业" prop="employmentCount" class="no-wrap-label">
              <el-input v-model.number="form.employmentCount" placeholder="请输入带动就业人数数量" type="number" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="支持金额" prop="financialSupport" class="no-wrap-label">
              <el-input v-model.number="form.financialSupport" placeholder="请输入资金支持金额" type="number" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="投入经费" prop="investmentAmount">
              <el-input v-model.number="form.investmentAmount" placeholder="请输入投入经费" type="number" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="整体面积" prop="totalArea">
              <el-input v-model.number="form.totalArea" placeholder="请输入整体面积" type="number" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="入驻日期" prop="entryDate">
              <el-date-picker v-model="form.entryDate" type="date" placeholder="请选择入驻日期" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
        <el-col :span="12">
          <el-form-item label="补贴金额" prop="governmentSubsidy" class="no-wrap-label">
            <el-input v-model.number="form.governmentSubsidy" placeholder="请输入政府补贴金额" type="number" />
          </el-form-item>
        </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input v-model="form.remark" type="textarea" placeholder="请输入内容"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>


  </div>
</template>

<script setup name="User">
import { getToken } from "@/utils/auth";
import { changeUserStatus, listUser, resetUserPwd, delUser, getUser, updateUser, addUser, deptTreeSelect } from "@/api/system/user";
import {addPark, delPark, getParkById, listPark, updatePark} from "@/api/system/park";
import {onMounted, ref} from "vue";
import {getRegionOptions} from "../../../api/system/region";
import {deleteIndustryList, getIndustryOptions} from "../../../api/system/industry";
import {deleteBatch} from "../../../api/system/park";

const router = useRouter();
const { proxy } = getCurrentInstance();
const { sys_normal_disable, sys_user_sex } = proxy.useDict("sys_normal_disable", "sys_user_sex");
const parkList = ref([]);
const userList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");
const dateRange = ref([]);
const deptName = ref("");
const deptOptions = ref(undefined);
const initPassword = ref(undefined);
const postOptions = ref([]);
const roleOptions = ref([]);
/*** 用户导入参数 */
const upload = reactive({
  // 是否显示弹出层（用户导入）
  open: false,
  // 弹出层标题（用户导入）
  title: "",
  // 是否禁用上传
  isUploading: false,
  // 是否更新已经存在的用户数据
  updateSupport: 0,
  // 设置上传的请求头部
  headers: { Authorization: "Bearer " + getToken() },
  // 上传的地址
  url: import.meta.env.VITE_APP_BASE_API + "/system/user/importData"
});
// 列显隐信息
const columns = ref([
  { key: 0, label: `用户编号`, visible: true },
  { key: 1, label: `用户名称`, visible: true },
  { key: 2, label: `用户昵称`, visible: true },
  { key: 3, label: `部门`, visible: true },
  { key: 4, label: `手机号码`, visible: true },
  { key: 5, label: `状态`, visible: true },
  { key: 6, label: `创建时间`, visible: true }
]);


const data = reactive({
  form: {
    parkId: undefined,
    parkName: undefined,
    industry: undefined,
    address: undefined,
    companyMembers: undefined,
    financialSupport: undefined,
    investmentAmount: undefined,
    totalArea: undefined,
    entryDate: undefined,
    governmentSubsidy: undefined,
    graduateCount: undefined,
    projectCount: undefined,
    traineeCount: undefined,
    employmentCount: undefined,
    createdAt: undefined,
    updatedAt: undefined,
    industryOptions:[],
  },
   queryParams: {
    pageNum: 1,
    pageSize: 10,
    parkName: undefined,
    industry: undefined,
    status: undefined
  },
  rules: {
    parkName: [{ required: true, message: "创业园名称不能为空", trigger: "blur" }],
    industry: [{ required: true, message: "所属行业不能为空", trigger: "blur" }],
    address: [{ required: true, message: "入驻地址不能为空", trigger: "blur" }],
    companyMembers: [
      { required: true, message: "团队成员数量不能为空", trigger: "blur" },
      { type: 'number', message: "团队成员数量必须为数字", trigger: "blur" }
    ],
    graduateCount: [
      { required: true, message: "创业毕业生数量不能为空", trigger: "blur" },
      {
        validator: (rule, value, callback) => {
          if (value === '' || value === null || value === undefined) {
            callback(new Error('创业毕业生数量不能为空'));
          } else if (isNaN(Number(value))) {
            callback(new Error('创业毕业生数量必须为数字'));
          } else {
            callback();
          }
        },
        trigger: ['blur', 'change']
      }
    ],
    projectCount: [
      { required: true, message: "创业项目数量不能为空", trigger: "blur" },
      { type: 'number', message: "创业项目数量必须为数字", trigger: "blur" }
    ],
    traineeCount: [
      { required: true, message: "培训学员数量不能为空", trigger: "blur" },
      {
        validator: (rule, value, callback) => {
          if (value === '' || value === null || value === undefined) {
            callback(new Error('培训学员数量不能为空'));
          } else if (isNaN(Number(value))) {
            callback(new Error('培训学员数量必须为数字'));
          } else {
            callback();
          }
        },
        trigger: ['blur', 'change']
      }
    ],
    employmentCount: [
      { required: true, message: "带动就业人数数量不能为空", trigger: "blur" },
      { type: 'number', message: "带动就业人数数量必须为数字", trigger: "blur" }
    ],
    financialSupport: [
      { required: true, message: "资金支持金额不能为空", trigger: "blur" },
      { type: 'number', message: "资金支持金额必须为数字", trigger: "blur" }
    ],
    investmentAmount: [
      { required: true, message: "投入经费不能为空", trigger: "blur" },
      { type: 'number', message: "投入经费必须为数字", trigger: "blur" }
    ],
    totalArea: [
      { required: true, message: "整体面积不能为空", trigger: "blur" },
      { type: 'number', message: "整体面积必须为数字", trigger: "blur" }
    ],
    governmentSubsidy: [
      { required: true, message: "政府补贴金额不能为空", trigger: "blur" },
      { type: 'number', message: "政府补贴金额必须为数字", trigger: "blur" }
    ]
  }

});

const { queryParams, form,rules } = toRefs(data);


/** 查询用户列表 */
function getList() {
  loading.value = true;
  listPark(proxy.addDateRange(queryParams.value, dateRange.value)).then(res => {
    loading.value = false;
    parkList.value = res.records;
    total.value = res.total;
  });
};
/** 节点单击事件 */
function handleNodeClick(data) {
  queryParams.value.deptId = data.id;
  handleQuery();
};
/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
};
/** 重置按钮操作 */
function resetQuery() {
  dateRange.value = [];
  proxy.resetForm("queryRef");
  queryParams.value.deptId = undefined;
  proxy.$refs.tree.setCurrentKey(null);
  handleQuery();
};
/** 批量删除按钮操作 */
function handleDeleteList() {
  const selectedRows = getSelectedRows(); // 获取选中的行数据
  if (!selectedRows || selectedRows.length === 0) {
    proxy.$modal.msgError("请选择要删除的数据项");
    return;
  }

  const parkIds = selectedRows.map(row => row.parkId);
  proxy.$modal.confirm(`是否确认删除标签编号为"${parkIds}"的数据项？`).then(function () {
    return deleteBatch(parkIds);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
};
/** 删除按钮操作 */
function handleDelete(row) {
  proxy.$modal.confirm('是否确认删除用户编号为"' + row.parkId + '"的数据项？').then(function () {
    return delPark(row.parkId);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
};
/** 导出按钮操作 */
function handleExport() {
  proxy.download("/student-entrepreneurship-park/export", {
    ...queryParams.value,
  },`student-entrepreneurship-park_${new Date().getTime()}.xlsx`);
};
/** 用户状态修改  */
function handleStatusChange(row) {
  let text = row.status === "0" ? "启用" : "停用";
  proxy.$modal.confirm('确认要"' + text + '""' + row.userName + '"用户吗?').then(function () {
    return changeUserStatus(row.userId, row.status);
  }).then(() => {
    proxy.$modal.msgSuccess(text + "成功");
  }).catch(function () {
    row.status = row.status === "0" ? "1" : "0";
  });
};
/** 更多操作 */
function handleCommand(command, row) {
  switch (command) {
    case "handleResetPwd":
      handleResetPwd(row);
      break;
    case "handleAuthRole":
      handleAuthRole(row);
      break;
    default:
      break;
  }
};
/** 跳转角色分配 */
function handleAuthRole(row) {
  const userId = row.userId;
  router.push("/system/user-auth/role/" + userId);
};
/** 重置密码按钮操作 */
function handleResetPwd(row) {
  proxy.$prompt('请输入"' + row.userName + '"的新密码', "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    closeOnClickModal: false,
    inputPattern: /^.{5,20}$/,
    inputErrorMessage: "用户密码长度必须介于 5 和 20 之间",
  }).then(({ value }) => {
    resetUserPwd(row.userId, value).then(response => {
      proxy.$modal.msgSuccess("修改成功，新密码是：" + value);
    });
  }).catch(() => {});
};
const multipleTable = ref(null);
const multipleSelection = ref([]);
/** 选择条数  */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.userId);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
  multipleSelection.value = selection; // 移除多余的点
}

// 获取选中的行数据
const getSelectedRows = () => {
  return multipleSelection.value;
};
/** 导入按钮操作 */
function handleImport() {
  upload.title = "用户导入";
  upload.open = true;
};
/** 下载模板操作 */
function importTemplate() {
  proxy.download("system/user/importTemplate", {
  }, `user_template_${new Date().getTime()}.xlsx`);
};
/**文件上传中处理 */
const handleFileUploadProgress = (event, file, fileList) => {
  upload.isUploading = true;
};
/** 文件上传成功处理 */
const handleFileSuccess = (response, file, fileList) => {
  upload.open = false;
  upload.isUploading = false;
  proxy.$refs["uploadRef"].handleRemove(file);
  proxy.$alert("<div style='overflow: auto;overflow-x: hidden;max-height: 70vh;padding: 10px 20px 0;'>" + response.msg + "</div>", "导入结果", { dangerouslyUseHTMLString: true });
  getList();
};
/** 提交上传文件 */
function submitFileForm() {
  proxy.$refs["uploadRef"].submit();
};
/** 重置操作表单 */
function reset() {
  form.value = {
    parkId: undefined,
    parkName: undefined,
    industry: undefined,
    address: undefined,
    companyMembers: undefined,
    financialSupport: undefined,
    investmentAmount: undefined,
    totalArea: undefined,
    entryDate: undefined,
    governmentSubsidy: undefined,
    graduateCount: undefined,
    projectCount: undefined,
    traineeCount: undefined,
    employmentCount: undefined,
    createdAt: undefined,
    updatedAt: undefined,
    industryOptions: form.value.industryOptions,
  };
  proxy.resetForm("userRef");
};
/** 取消按钮 */
function cancel() {
  open.value = false;
  reset();
};
/** 新增按钮操作 */
function handleAdd() {
  reset();
  getUser().then(response => {
    postOptions.value = response.posts;
    roleOptions.value = response.roles;
    open.value = true;
    title.value = "添加创业园";
    form.value.password = initPassword.value;
  });
};
/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
    if (row && row.parkId) {
      getParkById(row.parkId).then(response => {
        form.value = { ...response, industryOptions: form.value.industryOptions}
        console.log("form.value", form.value)
        open.value = true
        title.value = "修改创业园"
      }).catch(error => {
        console.error("修改创业园时出错：", error)
        proxy.$modal.msgError("修改创业园失败，请重试")
      })
    }
  }
/** 提交按钮 */
function submitForm() {
  proxy.$refs["parkRef"].validate(valid => {
    if (valid) {
      if (form.value.parkId != undefined) {
        updatePark(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addPark(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功");
          open.value = false;
          getList();
        });
      }
    }
  });
};
function getIndustryOption() {
  getIndustryOptions().then(res => {
    const industryNames = res.map(item => item.industryName)
    form.value.industryOptions = industryNames
    console.log("form.value.industryOptions", form.value.industryOptions)
  }).catch(error => {
    console.error(error)
  })
}
// 添加数字输入处理函数
const handleNumberInput = (value, field) => {
  if (value === '') {
    form.value[field] = undefined;
  } else {
    const num = Number(value);
    if (!isNaN(num)) {
      form.value[field] = num;
    }
  }
};
onMounted(() => {

  getList()
  getIndustryOption()
})
</script>

<style scoped>
.app-container {
  width: 100%;
  padding: 20px;
  box-sizing: border-box;
}

.full-width-table {
  width: 100%;
}

/* If you want to remove any default padding/margin */
.el-table {
  margin: 0;
  padding: 0;
}


.no-wrap-label :deep(.el-form-item__label) {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* Optional: Adjust the label width for the specific field if needed */
.no-wrap-label {
  --el-form-label-width: 140px !important;
}

/* Ensure dialog has proper width on mobile */
@media screen and (max-width: 768px) {
  :deep(.el-dialog) {
    width: 95% !important;
  }
}

.no-wrap-label .el-form-item__label {
  white-space: nowrap;
}

.park-form {
  /* 设置整体表单样式 */
  font-size: 14px;
}

.park-form :deep(.el-form-item__label) {
  /* 增加标签基础宽度 */
  min-width: 120px !important;
  font-size: 14px;
}

/* 为特定的长标签设置样式 */
.custom-label :deep(.el-form-item__label) {
  width: auto !important;
  min-width: 120px;
  padding-right: 12px;
  white-space: nowrap;
}

/* 调整表单项间距 */
.el-row {
  margin-bottom: 8px;
}

/* 确保输入框有足够空间 */
.el-form-item :deep(.el-input) {
  width: calc(100% - 8px);
}

/* 响应式调整 */
@media screen and (max-width: 768px) {
  .park-form :deep(.el-form-item__label) {
    font-size: 13px;
  }

  .custom-label :deep(.el-form-item__label) {
    min-width: 100px;
  }

  :deep(.el-dialog) {
    width: 95% !important;
  }
}
</style>