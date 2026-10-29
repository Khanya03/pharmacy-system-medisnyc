import { Navigate, Route, Routes } from "react-router-dom";
import ProtectedRoute from "./components/ProtectedRoute";
import About from "./pages/About";
import AddDoctor from "./pages/AddDoctor";
import AddMedicine from "./pages/AddMedicine";
import AddPatient from "./pages/AddPatient";
import AdminOverview from "./pages/AdminOverview";
import Appointments from "./pages/Appointments";
import AuditLog from "./pages/AuditLog";
import BookAppointment from "./pages/BookAppointment";
import ConsultationForm from "./pages/ConsultationForm";
import Contact from "./pages/Contact";
import Dashboard from "./pages/Dashboard";
import DoctorPatients from "./pages/DoctorPatients";
import DoctorPrescriptions from "./pages/DoctorPrescriptions";
import DoctorsList from "./pages/DoctorsList";
import ForgotPassword from "./pages/ForgotPassword";
import Home from "./pages/Home";
import InventoryDashboard from "./pages/InventoryDashboard";
import Login from "./pages/Login";
import MyOrders from "./pages/MyOrders";
import OrderList from "./pages/OrderList";
import PaymentCheckout from "./pages/PaymentCheckout";
import PaymentReceipt from "./pages/PaymentReceipt";
import PatientsList from "./pages/PatientsList";
import PrescriptionQueue from "./pages/PrescriptionQueue";
import PrescriptionTracking from "./pages/PrescriptionTracking";
import Profile from "./pages/Profile";
import ReadyForCollection from "./pages/ReadyForCollection";
import Register from "./pages/Register";
import Reports from "./pages/Reports";
import ResetPassword from "./pages/ResetPassword";
import Services from "./pages/Services";
import TodaysAppointments from "./pages/TodaysAppointments";
import UserManagement from "./pages/UserManagement";

const ADMIN = ["ADMIN"];
const PHARMACY = ["ADMIN", "PHARMACIST"];
const CLINICAL = ["ADMIN", "DOCTOR"];
const STAFF = ["ADMIN", "DOCTOR", "PHARMACIST"];
const PAYING_STAFF = ["ADMIN", "PHARMACIST", "PATIENT"];

function secure(page, roles) {
  return <ProtectedRoute roles={roles}>{page}</ProtectedRoute>;
}

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Home />} />
      <Route path="/about" element={<About />} />
      <Route path="/services" element={<Services />} />
      <Route path="/contact" element={<Contact />} />
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route path="/forgot-password" element={<ForgotPassword />} />
      <Route path="/reset-password" element={<ResetPassword />} />
      <Route path="/dashboard" element={secure(<Dashboard />)} />
      <Route path="/my-orders" element={secure(<MyOrders />, ["PATIENT"])} />
      <Route path="/orders/:orderId/pay" element={secure(<PaymentCheckout />, PAYING_STAFF)} />
      <Route path="/receipts/:paymentId" element={secure(<PaymentReceipt />, ["ADMIN", "PHARMACIST", "PATIENT"])} />
      <Route path="/profile" element={secure(<Profile />)} />
      <Route path="/appointments" element={secure(<Appointments />, ["ADMIN", "DOCTOR", "PHARMACIST", "PATIENT"])} />
      <Route path="/appointments/book" element={secure(<BookAppointment />, ["PATIENT"])} />
      <Route path="/consultations" element={secure(<TodaysAppointments />, CLINICAL)} />
      <Route path="/consultations/new" element={secure(<ConsultationForm />, ["DOCTOR"])} />
      <Route path="/consultations/patients" element={secure(<DoctorPatients />, CLINICAL)} />
      <Route path="/prescriptions/mine" element={secure(<DoctorPrescriptions />, STAFF)} />
      <Route path="/prescriptions/track" element={secure(<PrescriptionTracking />, ["PATIENT"])} />
      <Route path="/prescriptions/queue" element={secure(<PrescriptionQueue />, PHARMACY)} />
      <Route path="/prescriptions/ready" element={secure(<ReadyForCollection />, PHARMACY)} />
      <Route path="/orders" element={secure(<OrderList />, PHARMACY)} />
      <Route path="/admin/orders" element={secure(<OrderList />, PHARMACY)} />
      <Route path="/inventory" element={secure(<InventoryDashboard />, PHARMACY)} />
      <Route path="/inventory/add" element={secure(<AddMedicine />, PHARMACY)} />
      <Route path="/admin" element={secure(<AdminOverview />, ADMIN)} />
      <Route path="/admin/users" element={secure(<UserManagement />, ADMIN)} />
      <Route path="/admin/audit-log" element={secure(<AuditLog />, ADMIN)} />
      <Route path="/admin/reports" element={secure(<Reports />, ADMIN)} />
      <Route path="/reports" element={secure(<Reports />, PHARMACY)} />
      <Route path="/admin/doctors" element={secure(<DoctorsList />, ADMIN)} />
      <Route path="/admin/doctors/add" element={secure(<AddDoctor />, ADMIN)} />
      <Route path="/admin/patients" element={secure(<PatientsList />, ADMIN)} />
      <Route path="/admin/patients/add" element={secure(<AddPatient />, ADMIN)} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
