<template>
  <div class="enterprise-form-container">
    <div class="enterprise-form-wrapper">
      <h2 class="title">企业申请表</h2>
      <form @submit.prevent="submitForm" class="enterprise-form">
        <div class="form-row">
          <div class="form-group">
            <label for="name">姓名</label>
            <input type="text" id="name" v-model="formData.applicantName" required>
          </div>

          <div class="form-group">
            <label for="phone">电话</label>
            <input type="tel" id="phone" v-model="formData.applicantPhone" required>
          </div>

          <div class="form-group">
            <label for="email">邮箱</label>
            <input type="email" id="email" v-model="formData.applicantEmail" required>
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label for="applicationType">申请类型</label>
            <select id="applicationType" v-model="formData.applicationType" required>
              <option value="">请选择</option>
              <option v-for="option in applicationTypeOptions" :key="option.value" :value="option.value">
                {{ option.label }}
              </option>
            </select>
          </div>

          <div class="form-group">
            <label for="industryName">行业类型</label>
            <select id="industryName" v-model="formData.industryName" required>
              <option value="">请选择</option>
              <option v-for="option in industryOptions" :key="option.value" :value="option.value">
                {{ option.label }}
              </option>
            </select>
          </div>
        </div>

        <div class="form-group description-group">
          <label for="enterpriseDescription">企业介绍</label>
          <textarea id="enterpriseDescription" v-model="formData.enterpriseDescription" rows="16" required></textarea>
        </div>

        <button type="submit" class="submit-button" :disabled="isSubmitting">
          <span v-if="!isSubmitting">提交申请</span>
          <span v-else class="spinner"></span>
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import axios from 'axios';
import {listAllApplicationType, submitApplication} from "@/api/application/applicationType";

const formData = reactive({
  applicantName: '',
  applicantPhone: '',
  applicantEmail: '',
  applicationType: '',
  industryName: '',
  enterpriseDescription: ''
});

const industryOptions = ref([]);
const applicationTypeOptions = ref([]);
const isSubmitting = ref(false);

const getIndustryOptions = async () => {
  try {
    const response = await axios.get('http://localhost:8091/industries/getIndustryOptions');
    industryOptions.value = response.data.map(item => ({
      value: item.industryId,
      label: item.industryName
    }));


  } catch (error) {
    console.error("Error fetching industry options:", error);
  }
};

const getApplicationTypeOptions = async () => {
  try {
    const response = await listAllApplicationType();
    applicationTypeOptions.value = response.map(item => ({
      value: item.applicationTypeId,
      label: item.applicationName
    }));
  } catch (error) {
    console.error("Error fetching application type options:", error);
  }
};

const submitForm = async () => {
  isSubmitting.value = true;
  const data = {
    ...formData,
    applicationDate: new Date().toISOString(),
    status: 'pending',
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString()
  };

  try {
    const response = await submitApplication(data);
    if (response.data) {
      alert('申请已提交！');
      // 清空表单
      Object.keys(formData).forEach(key => {
        formData[key] = '';
      });
    } else {
      alert('提交失败，请稍后再试。');
    }
  } catch (error) {
    console.error('Error submitting form:', error);
    alert('提交失败，请稍后再试。');
  } finally {
    isSubmitting.value = false;
  }
};

onMounted(() => {
  getIndustryOptions();
  getApplicationTypeOptions();
});
</script>

<style scoped>
.enterprise-form-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 2rem;
}

.enterprise-form-wrapper {
  background-color: white;
  padding: 2rem;
  border-radius: 12px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 800px;
  transition: transform 0.3s ease;
}

.enterprise-form-wrapper:hover {
  transform: translateY(-5px);
}

.title {
  color: #2c3e50;
  margin-bottom: 2rem;
  text-align: center;
  font-size: 2.5rem;
  font-weight: bold;
  text-shadow: 2px 2px 4px rgba(0,0,0,0.1);
}

.enterprise-form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.form-row {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  flex: 1;
}

label {
  margin-bottom: 0.5rem;
  color: #34495e;
  font-weight: 600;
  font-size: 0.9rem;
}

input, select, textarea {
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 1rem;
  transition: all 0.3s ease;
  width: 100%;
}

input:focus, select:focus, textarea:focus {
  outline: none;
  border-color: #3498db;
  box-shadow: 0 0 0 2px rgba(52, 152, 219, 0.2);
}

textarea {
  resize: vertical;
  min-height: 120px;
}

.submit-button {
  padding: 1rem;
  background-color: #3498db;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1.1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.submit-button:hover {
  background-color: #2980b9;
}

.submit-button:disabled {
  background-color: #bdc3c7;
  cursor: not-allowed;
}

.spinner {
  display: inline-block;
  width: 20px;
  height: 20px;
  border: 3px solid rgba(255,255,255,.3);
  border-radius: 50%;
  border-top-color: #fff;
  animation: spin 1s ease-in-out infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

@media (max-width: 768px) {
  .form-row {
    flex-direction: column;
  }

  .enterprise-form-wrapper {
    padding: 1.5rem;
  }

  .title {
    font-size: 2rem;
  }
}

/* Animated background */
@keyframes gradientBG {
  0% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0% 50%;
  }
}

.enterprise-form-container {
  background: linear-gradient(-45deg, #ee7752, #e73c7e, #23a6d5, #23d5ab);
  background-size: 400% 400%;
  animation: gradientBG 15s ease infinite;
}

/* Input and textarea animations */
@keyframes inputFocus {
  0% { transform: scale(1); }
  50% { transform: scale(1.02); }
  100% { transform: scale(1); }
}

input:focus, select:focus, textarea:focus {
  animation: inputFocus 0.3s ease;
}

/* Button hover effect */
.submit-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(
      120deg,
      transparent,
      rgba(255, 255, 255, 0.4),
      transparent
  );
  transition: all 0.6s;
}

.submit-button:hover::before {
  left: 100%;
}
</style>