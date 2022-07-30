import {CAlert, CButton, CCard, CCardBody, CCardHeader, CListGroup, CListGroupItem} from "@coreui/react-pro";
import CIcon from "@coreui/icons-react";
import {cilChatBubble, cilGlobeAlt, cilPeople, cilPlus} from "@coreui/icons";
import {useNavigate} from "react-router-dom";

export default function Peers(props) {
  const navigate = useNavigate();
  const peerToGroups = props.peerToGroups;
  const peers = [];
  peerToGroups.forEach((groupsSet, peerId) => {
    const groupsArray = [];
    groupsSet.forEach(group => groupsArray.push(group));
    const peerObject = {
      peerId: peerId,
      groups: groupsArray,
    }
    peers.push(peerObject);
  });
  return <>
    <CCard>
      <CCardHeader className="d-flex align-items-center">
        <CIcon icon={cilPeople}/>&nbsp;<b>Peers</b>
        <div className="ms-auto">
        </div>
      </CCardHeader>
      <CCardBody>
        <CListGroup>
          {peers.length == 0 ? <CAlert color="info">You are not connected to any peer.</CAlert> : ''}
          {peers.map((peer, index) => <CListGroupItem key={index.toString()} className="d-flex justify-content-between align-items-center">
            {peer.peerId}
            <CButton size="sm" color="secondary" onClick={() => navigate(`/chat/${peer.peerId}`)}><CIcon icon={cilChatBubble}/>&nbsp;Chat</CButton>
            </CListGroupItem>
          )}
        </CListGroup>
      </CCardBody>
    </CCard>
  </>;
}
