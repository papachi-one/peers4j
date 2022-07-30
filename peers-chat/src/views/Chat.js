import {useParams} from "react-router-dom";
import {CButton, CCard, CCardBody, CCardFooter, CCardHeader, CForm, CFormInput} from "@coreui/react-pro";
import CIcon from "@coreui/icons-react";
import {cilChatBubble} from "@coreui/icons";
import {useEffect, useRef} from "react";

export default function Chat(props) {
  const {peerId} = useParams();
  const peerToName = props.peerToName;
  const name = peerToName.has(peerId) ? peerToName.get(peerId) : peerId;
  const sendMessage = props.sendMessage;
  const messages = props.messages;
  const didRead = props.didRead;
  const clearChat = props.clearChat;
  const myMessages = [];
  messages.forEach(message => {
    if (message.peerId == peerId) {
      myMessages.push(message);
    }
  });
  const refText = useRef();
  const chatBody = useRef();
  const scroll = () => {
    if (chatBody.current) chatBody.current.scrollIntoView({block: 'start'});
  }
  const scrollSmooth = () => {
    if (chatBody.current) chatBody.current.scrollIntoView({behavior: 'smooth', block: 'start'});
  }
  useEffect(() => {
    didRead(peerId);
    scroll();
    if (refText.current) refText.current.focus();
  }, [peerId]);
  useEffect(() => {
    scrollSmooth();
    didRead(peerId);
  }, [messages]);
  return <>
    <CCard>
      <CCardHeader className="d-flex align-items-center">
        <CIcon icon={cilChatBubble}/>&nbsp;<b>Chat with {name}</b>
        <div className="ms-auto">
          <CButton size="sm" onClick={() => clearChat(peerId)}>Clear chat</CButton>
        </div>
      </CCardHeader>
      <CCardBody style={{height: 'calc(100vh - 305px)', overflowY: "auto"}}>
        {myMessages.map((message, index) =>
          <div
            className={message.received ? "d-flex flex-row justify-content-start" : "d-flex flex-row justify-content-end"}
            ref={myMessages.length - 1 == index ? chatBody : undefined}>
            <p
              className={message.received ? "small p-2 mb-1 rounded-3 bg-light" : "small p-2 mb-1 rounded-3 bg-primary text-white"}>{message.message}
            </p>
          </div>)}
        {/*<div className="divider d-flex align-items-center mb-4">*/}
        {/*  <p className="text-center mx-3 mb-0" style={{color: '#a2aab7'}}>Today</p>*/}
        {/*</div>*/}
      </CCardBody>
      <CCardFooter>
        <CForm onSubmit={event => {
          event.preventDefault();
          const string = refText.current.value;
          refText.current.value = null;
          sendMessage(peerId, string);
        }}>
          <CFormInput ref={refText} placeholder="Type message here..."/>
        </CForm>
      </CCardFooter>
    </CCard>
  </>;
}
