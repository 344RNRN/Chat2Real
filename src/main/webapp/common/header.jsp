
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>

<style>
    .header {
        width: 100%;
        min-height: 85px;
        padding: 0 4%;
        box-sizing: border-box;

        display: flex;
        align-items: center;
        justify-content: space-between;

        background: #ffffff;
        border-bottom: 1px solid #e8edf5;
        box-shadow: 0 3px 15px rgba(32, 54, 83, 0.04);
    }

    .header-left {
        display: flex;
        align-items: center;
        gap: 22px;
    }

    .logo {
        font-family: Georgia, serif;
        font-size: 32px;
        font-weight: bold;
        color: #203653;
        text-decoration: none;
        white-space: nowrap;
    }

    .logo span {
        color: #f582ad;
    }

    .header-divider {
        height: 32px;
        border-left: 1px solid #cbd5e2;
    }

    .system-name {
        margin: 0;
        color: #203653;
        font-size: 17px;
        font-weight: 600;
    }

    .header-nav {
        display: flex;
        align-items: center;
    }

    .admin-label {
        display: inline-flex;
        align-items: center;
        gap: 8px;

        padding: 12px 18px;

        color: #ed477a;
        background: #fff0f5;
        border-radius: 10px;

        font-size: 14px;
        font-weight: 700;
        white-space: nowrap;
    }

    .admin-label span {
        color: #f45c8b;
    }

    @media (max-width: 600px) {
        .header {
            padding: 18px 20px;
            flex-wrap: wrap;
            gap: 15px;
        }

        .header-left {
            gap: 12px;
            flex-wrap: wrap;
        }

        .logo {
            font-size: 27px;
        }

        .system-name {
            font-size: 14px;
        }

        .header-divider {
            height: 25px;
        }
    }
</style>

<header class="header">

    <div class="header-left">

        <a href="${pageContext.request.contextPath}/admin/login.jsp"
           class="logo">
            Chat2<span>Real</span>
        </a>

        <div class="header-divider"></div>

        <h1 class="system-name">
            ～チャットからリアルな交流へ
        </h1>

    </div>

    <nav class="header-nav">
        
    </nav>

</header>
