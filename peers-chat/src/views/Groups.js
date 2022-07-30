import {
  CAlert,
  CButton,
  CCard,
  CCardBody,
  CCardHeader,
  CForm,
  CFormInput,
  CInputGroup,
  CInputGroupText,
  CListGroup,
  CListGroupItem,
  CModal,
  CModalBody,
  CModalFooter,
  CModalHeader,
  CModalTitle,
  CRow
} from "@coreui/react-pro";
import CIcon from "@coreui/icons-react";
import {cilGlobeAlt, cilPlus} from "@coreui/icons";
import {useRef, useState} from "react";

export default function Groups(props) {
  const [showModal, setShowModal] = useState(false);
  const groups = props.groups;
  const addCallback = props.add;
  const delCallback = props.del;

  const refGroup = useRef();

  return <>
    <CCard>
      <CCardHeader className="d-flex align-items-center">
        <CIcon icon={cilGlobeAlt}/>&nbsp;<b>Groups</b>
        <div className="ms-auto">
          <CButton size="sm" color="success" onClick={() => {
            setShowModal(true);
          }}><CIcon icon={cilPlus}/>&nbsp;Join group</CButton>
        </div>
      </CCardHeader>
      <CCardBody>
        <CListGroup>
          {groups.length == 0 ? <CAlert color="info">You have not joined any group yet.</CAlert> : ''}
          {groups.map((group, index) => <CListGroupItem className="d-flex justify-content-between align-items-center">
              {group}
              <CButton size="sm" color="danger" onClick={() => delCallback(group)}>Leave group</CButton>
            </CListGroupItem>
          )}
        </CListGroup>
      </CCardBody>
    </CCard>
    <CModal alignment="center" visible={showModal} onClose={() => setShowModal(false)}>
      <CForm onSubmit={event => {
        event.preventDefault();
        const group = refGroup.current.value;
        addCallback(group);
        setShowModal(false);
      }}>
        <CModalHeader onClose={() => setShowModal(false)}>
          <CModalTitle>
            Join group
          </CModalTitle>
        </CModalHeader>
        <CModalBody>
          <CRow className="mb-2">
            <CInputGroup>
              <CInputGroupText>
                <CIcon icon={cilGlobeAlt}/>
              </CInputGroupText>
              <CFormInput ref={refGroup} placeholder="Group name" name="group" id="test"/>
            </CInputGroup>
          </CRow>
        </CModalBody>
        <CModalFooter>
          <CRow>
            <CButton type="submit">Join</CButton>
          </CRow>
        </CModalFooter>
      </CForm>
    </CModal>
  </>;
}
