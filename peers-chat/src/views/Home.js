import {
  CButton,
  CCard,
  CCardBody,
  CCardFooter,
  CCardHeader,
  CContainer,
  CForm,
  CFormInput,
  CInputGroup,
  CInputGroupText,
  CRow
} from "@coreui/react-pro";
import CIcon from "@coreui/icons-react";
import {useRef} from "react";
import {cilHome, cilInputPower, cilStorage, cilUser} from "@coreui/icons";

export default function Home(props) {
  const name = props.name;
  const url = props.url;
  const isConnected = props.isConnected;
  const connectCallback = props.connect;
  const disconnectCallback = props.disconnect;

  const refName = useRef();
  const refUrl = useRef();

  const submit = event => {
    event.preventDefault();
    const name = refName.current.value;
    const url = refUrl.current.value;
    connectCallback(name, url);
  };
  const disconnect = () => disconnectCallback();

  return <CContainer className="d-flex justify-content-center">
    <div className="col-md-10 col-lg-8 col-xl-6">
      <CForm onSubmit={submit}>
        <CCard>
          <CCardHeader className="d-flex align-items-center">
            <CIcon icon={cilHome}/>&nbsp;<b>Home</b>
            <div className="ms-auto">
              <CButton size="sm" style={{visibility: isConnected ? '' : 'hidden'}} onClick={disconnect}>Disconnect</CButton>
            </div>
          </CCardHeader>
          <CCardBody>
            <CRow className="mb-2">
              <CInputGroup>
                <CInputGroupText>
                  <CIcon icon={cilUser}/>
                </CInputGroupText>
                <CFormInput ref={refName} placeholder="Name" name="name" defaultValue={name}/>
              </CInputGroup>
            </CRow>
            <CRow>
              <CInputGroup>
                <CInputGroupText>
                  <CIcon icon={cilStorage}/>
                </CInputGroupText>
                <CFormInput ref={refUrl} placeholder="Server URL" name="url" defaultValue={url}/>
              </CInputGroup>
            </CRow>
          </CCardBody>
          <CCardFooter>
            <CRow>
              <CButton type="submit"><CIcon icon={cilInputPower}/>&nbsp;Connect</CButton>
            </CRow>
          </CCardFooter>
        </CCard>
      </CForm>
    </div>
  </CContainer>;
}
