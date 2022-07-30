import React from 'react'

import {CBreadcrumb, CBreadcrumbItem} from '@coreui/react-pro'

const AppBreadcrumb = () => {
  return (
    <CBreadcrumb className="m-0 ms-2">
      <CBreadcrumbItem href="/">Home</CBreadcrumbItem>
    </CBreadcrumb>
  )
}

export default React.memo(AppBreadcrumb)
