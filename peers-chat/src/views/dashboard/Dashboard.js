import React, {useEffect, useRef, useState} from 'react'
import {
  CAlert,
  CButton,
  CCard,
  CCardBody, CCardFooter,
  CCardHeader,
  CCardTitle,
  CContainer,
  CForm,
  CFormInput,
  CFormText, CHeaderDivider, CRow,
} from '@coreui/react-pro'
import CIcon from '@coreui/icons-react'
import {cilChatBubble, cilSend} from '@coreui/icons'
import useWebSocket, {ReadyState} from 'react-use-websocket'

const Dashboard = () => {
  const [url, setUrl] = useState(
    'ws://localhost:8080/ws/events/00000000-0000-0000-0000-000000000002',
  )
  const {sendMessage, lastMessage, lastJsonMessage, readyState} = useWebSocket(url)
  useEffect(() => {
    console.log(readyState)
    if (readyState == ReadyState.OPEN) {
      sendMessage(
        JSON.stringify({
          messageType: 'JOIN',
          groupId: '00000000-0000-0000-0000-00000000000a',
          groupRole: 'PEER',
        }),
      )
    }
  }, [readyState])
  const [messages, setMessages] = useState([])
  const chatInput = useRef()
  const chatBody = useRef()
  useEffect(() => {
    if (lastMessage !== null) {
      const message = lastJsonMessage
      const messageType = message.messageType
      if (messageType == 'DATA') {
        const peerId = message.peerId
        const base64Encoded = message.data
        console.log(base64Encoded)
        const ascii = window.atob(base64Encoded)
        console.log(ascii)
        const array = Uint8Array.from(ascii, (c) => c.charCodeAt(0))
        console.log(array)
        const string = new TextDecoder().decode(array)
        console.log(typeof string)
        setMessages((messages) => [...messages, string])
      }
    }
  }, [lastJsonMessage])
  useEffect(() => {
    // chatBody.current.scrollIntoView({ behavior: 'smooth' })
  }, [messages])
  // useEffect(() => chatInput.current.focus())
  useEffect(() => {
    const eventSource = new EventSource(
      'http://localhost:8080/events/00000000-0000-0000-0000-000000000001',
    )
    eventSource.addEventListener('open', (event) => {
      fetch(
        'http://localhost:8080/peers/00000000-0000-0000-0000-000000000001/groups/00000000-0000-0000-0000-00000000000a',
        {method: 'POST'},
      ).catch((error) => console.log(error))
    })
    eventSource.addEventListener('message', (messageEvent) => {
      console.log(messageEvent.data)
    })
    return () => eventSource.close()
  }, [])
  if (true) {
    return (
      <CContainer fluid>
        <CCard>
          <CCardHeader className="d-flex justify-content-between align-items-center">
            <h5 className="mb-0">Chat</h5>
            <CButton size="sm">App</CButton>
          </CCardHeader>
          <CCardBody style={{height: 'calc(100vh - 320px)', overflowY: "auto"}}>
            <div className="d-flex flex-row justify-content-start">
              <div>
                <p className="small p-2 ms-3 mb-1 rounded-3" style={{backgroundColor: '#f5f6f7'}}>Hi</p>
                <p className="small p-2 ms-3 mb-1 rounded-3" style={{backgroundColor: '#f5f6f7'}}>How are you ...???</p>
                <p className="small p-2 ms-3 mb-1 rounded-3" style={{backgroundColor: '#f5f6f7'}}>What are you doing
                  tomorrow? Can we come up a bar?</p>
                <p className="small ms-3 mb-3 rounded-3 text-muted">23:58</p>
              </div>
            </div>
            <div className="divider d-flex align-items-center mb-4">
              <p className="text-center mx-3 mb-0" style={{color: '#a2aab7'}}>Today</p>
            </div>
            <div className="d-flex flex-row justify-content-end mb-4 pt-1">
              <div>
                <p className="small p-2 me-3 mb-1 text-white rounded-3 bg-primary">Hiii, I'm good.</p>
                <p className="small p-2 me-3 mb-1 text-white rounded-3 bg-primary">How are you doing?</p>
                <p className="small p-2 me-3 mb-1 text-white rounded-3 bg-primary">Long time no see! Tomorrow
                  office. will
                  be free on sunday.</p>
                <p className="small me-3 mb-3 rounded-3 text-muted d-flex justify-content-end">00:06</p>
              </div>
            </div>
          </CCardBody>
          <CCardFooter>
            <CFormInput placeholder="Type message here..."/>
          </CCardFooter>
        </CCard>
      </CContainer>
    )
  }
  return (
    <CContainer fluid={true} className="h-100">
      <CCard className="h-100">
        <CCardHeader>
          <CIcon icon={cilChatBubble}/>
          &nbsp;
          <b>Chat</b>
        </CCardHeader>
        <CCardBody>
          <div ref={chatBody} className="hh overflow-auto">
            {messages.map((item, index) => (
              <div key={index.toString()} ref={messages.length - 1 == index ? chatBody : undefined}>
                <CAlert color="secondary" className="w-50">
                  {item}
                </CAlert>
              </div>
            ))}
          </div>
          <CForm
            onSubmit={(event) => {
              event.preventDefault()
              const value = chatInput.current.value
              chatInput.current.value = null

              const encoded = new TextEncoder().encode(value)
              let string = ''
              for (let i = 0; i < encoded.length; i++) string += String.fromCodePoint(encoded.at(i))
              const base64Encoded = window.btoa(string)

              sendMessage(
                JSON.stringify({
                  messageType: 'DATA',
                  peerId: '00000000-0000-0000-0000-000000000002',
                  data: base64Encoded,
                }),
              )
            }}
          >
            <div className="d-flex">
              <CFormInput ref={chatInput} placeholder="Type here message..."/>
              <CButton size="sm" color="secondary" className="ms-1" type="submit">
                <CIcon icon={cilSend}/>
              </CButton>
            </div>
          </CForm>
        </CCardBody>
      </CCard>
    </CContainer>
  )
}

export default Dashboard
