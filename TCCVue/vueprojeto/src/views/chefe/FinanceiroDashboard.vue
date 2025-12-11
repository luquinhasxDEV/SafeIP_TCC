<template>
    <div>
        <div class="page-container">
            <SidebarGerente />

            <main class="dashboard-content">
                <div v-if="isLoading" class="loading-state">Carregando dados do servidor...</div>
                <div v-else-if="error" class="error-state">{{ error }}</div>

                <div v-else>
                    <section class="data-grid">
                        <div class="data-card employee-card">
                            <h2 class="card-title">Funcionários que mais venderam</h2>
                            <div v-for="employee in topEmployees" :key="employee.id" class="employee-item">
                                <img :src="employee.image" alt="Foto do Funcionário" class="employee-img">
                                <p class="employee-info">
                                    <span class="employee-name">{{ employee.name }}</span>
                                    <span class="employee-sales">R$ {{ employee.sales ?
                                        employee.sales.toLocaleString('pt-BR', { minimumFractionDigits: 2 }) : '0,00'
                                        }}</span>
                                </p>
                            </div>
                        </div>
                    </section>
                </div>
            </main>
        </div>
    </div>
</template>

<script setup>
import SidebarGerente from '@/components/sidebarGerente.vue';
import { ref, onMounted } from 'vue';
import axios from 'axios';

const topEmployees = ref([]);
const topItems = ref([]);
const isLoading = ref(true);
const error = ref(null);

const API_BASE_URL = 'http://localhost:8080/api/dashboard';

const fetchDashboardData = async () => {
    isLoading.value = true;
    error.value = null;

    try {
        const employeesResponse = await axios.get(`${API_BASE_URL}/funcionarios`);
        topEmployees.value = employeesResponse.data.map(emp => ({
            id: emp.id,
            name: emp.nome,
            sales: emp.valorVendido, 
            image: emp.urlImagem || 'https://i.pravatar.cc/150?img=' + emp.id
        }));

        const itemsResponse = await axios.get(`${API_BASE_URL}/itens`);
        topItems.value = itemsResponse.data.map(item => ({
            id: item.id,
            total: item.totalVendido, 
            image: item.urlImagem || 'https://i.pravatar.cc/150?img=' + item.id
        }));

    } catch (err) {
        console.error("Erro ao buscar dados do dashboard:", err);
        error.value = "Não foi possível carregar os dados. Verifique a API.";
        topEmployees.value = [];
        topItems.value = [];
    } finally {
        isLoading.value = false;
    }
};

onMounted(() => {
    fetchDashboardData();
});
</script>

<style scoped>
.page-container {
    display: flex;
    height: 100vh;
    width: 100vw;
    background-color: #f0f2f5;
}

.dashboard-content {
    flex-grow: 1;
    padding: 30px;
    background-color: #f0f2f5;
    overflow-y: auto;
}

.data-grid {
    display: grid;
    grid-template-columns: 1fr 1fr 1.2fr;
    gap: 30px;
}

.data-card {
    background-color: #3498db;
    color: white;
    padding: 25px;
    border-radius: 10px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.card-title {
    font-size: 1.4rem;
    font-weight: 700;
    margin-bottom: 20px;
    border-bottom: 1px solid rgba(255, 255, 255, 0.2);
    padding-bottom: 10px;
}

.employee-item {
    display: flex;
    align-items: center;
    padding: 10px 0;
}

.employee-img {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    margin-right: 15px;
    object-fit: cover;
}

.employee-info {
    display: flex;
    flex-direction: column;
    font-size: 0.95rem;
}

.employee-name {
    font-weight: 600;
}

.employee-sales {
    font-size: 0.9rem;
    font-weight: 300;
}
</style>
