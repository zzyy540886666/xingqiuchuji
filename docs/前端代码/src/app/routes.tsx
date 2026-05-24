import { createBrowserRouter } from "react-router";
import { Layout } from "./components/Layout";
import Home from "./pages/Home";
import Category from "./pages/Category";
import Community from "./pages/Community";
import Profile from "./pages/Profile";
import Search from "./pages/Search";
import ProductDetail from "./pages/ProductDetail";
import OrderConfirm from "./pages/OrderConfirm";
import MembershipCenter from "./pages/MembershipCenter";

export const router = createBrowserRouter([
  {
    path: "/",
    Component: Layout,
    children: [
      { index: true, Component: Home },
      { path: "category", Component: Category },
      { path: "community", Component: Community },
      { path: "profile", Component: Profile },
    ],
  },
  {
    path: "/search",
    Component: Search,
  },
  {
    path: "/product/:id",
    Component: ProductDetail,
  },
  {
    path: "/order/confirm",
    Component: OrderConfirm,
  },
  {
    path: "/membership/center",
    Component: MembershipCenter,
  },
  {
    path: "*",
    Component: () => <div>Not Found</div>
  }
]);
