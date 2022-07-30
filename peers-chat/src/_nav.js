import React from 'react'
import CIcon from '@coreui/icons-react'
import {cilChatBubble, cilGroup, cilHome, cilSpeedometer, cilStar} from '@coreui/icons'
import {CNavGroup, CNavItem} from '@coreui/react-pro'

const _nav = [
  {
    component: CNavItem,
    name: 'Home',
    to: '/home',
    badge: {
      color: 'danger-gradient',
      text: <>&nbsp;</>,
    },
    icon: <CIcon icon={cilHome} customClassName="nav-icon" />,
  },
  {
    component: CNavGroup,
    name: 'Groups',
    icon: <CIcon icon={cilGroup} customClassName="nav-icon" />,
    items: [
      {
        component: CNavItem,
        name: 'Group #1',
        to: '/group1',
      },
    ],
  },
  {
    component: CNavGroup,
    name: 'Chat',
    icon: <CIcon icon={cilChatBubble} customClassName="nav-icon" />,
    items: [
      {
        component: CNavItem,
        name: 'Papa',
        to: '/papa',
      },
      {
        component: CNavItem,
        name: 'Pepe',
        to: '/pepe',
      },
      {
        component: CNavItem,
        name: 'Bebe',
        to: '/Bebe',
        badge: {
          color: 'danger-gradient',
          text: '1',
        },
      },
    ],
  },
]

export default _nav
