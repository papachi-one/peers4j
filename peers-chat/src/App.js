import React, {useEffect, useState} from 'react'
import {Route, Routes, useLocation, useNavigate} from 'react-router-dom'
import './scss/style.scss'
import {AppHeader} from "./components";
import {CContainer, CFooter, CNavItem, CNavTitle, CSidebar, CSidebarBrand, CSidebarNav} from "@coreui/react-pro";
import CIcon from "@coreui/icons-react";
import {logoNegative} from "./assets/brand/logo-negative";
import {sygnet} from "./assets/brand/sygnet";
import SimpleBar from "simplebar-react";
import {AppSidebarNav} from "./components/AppSidebarNav";
import {useDispatch, useSelector} from "react-redux";
import Home from "./views/Home";
import useWebSocket, {ReadyState} from "react-use-websocket";
import {cilCommentBubble, cilGlobeAlt, cilHome, cilPeople} from "@coreui/icons";
import Groups from "./views/Groups";
import Peers from "./views/Peers";
import Chat from "./views/Chat";

async function getUuidFromString(string) {
  const data = new TextEncoder().encode(string);
  const digest = await crypto.subtle.digest('SHA-256', data);
  const array = Array.from(new Uint8Array(digest));
  const hex = array.map((b) => b.toString(16).padStart(2, '0')).join('');
  const uuid = hex.slice(0, 8) + '-' + hex.slice(8, 12) + '-' + hex.slice(12, 16) + "-" + hex.slice(16, 20) + "-" + hex.slice(20, 32);
  return uuid;
}

export default function App() {
  const navigate = useNavigate();
  const location = useLocation();
  const [name, setName] = useState();
  const [url, setUrl] = useState('wss://chat.papachi.one');
  const [webSocketUrl, setWebSocketUrl] = useState(null);
  const {
    sendMessage: sendWebSocketMessage, lastMessage: lastWebSocketMessage, readyState: webSocketState
  } = useWebSocket(webSocketUrl, {shouldReconnect: () => true});
  const [groups, setGroups] = useState(['home']);
  const [groupToPeers, setGroupToPeers] = useState(new Map());
  const [peerToGroups, setPeerToGroups] = useState(new Map());
  const [peerToName, setPeerToName] = useState(new Map());
  const [messages, setMessages] = useState([]);
  const [peerToUnread, setPeerToUnread] = useState(new Map());

  useEffect(() => navigate('/home'), []);

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

  const processJoin = (peerId, groupId) => {
    const peerToGroupsNew = new Map(peerToGroups);
    const groups = peerToGroupsNew.has(peerId) ? peerToGroupsNew.get(peerId) : new Set();
    groups.add(groupId);
    peerToGroupsNew.set(peerId, groups);
    setPeerToGroups(peerToGroupsNew);

    const groupToPeersNew = new Map(groupToPeers);
    const peers = groupToPeersNew.has(groupId) ? groupToPeersNew.get(groupId) : new Set();
    peers.add(peerId);
    groupToPeersNew.set(groupId, peers);
    setGroupToPeers(groupToPeersNew);

    const peerToUnreadNew = new Map(peerToUnread);
    peerToUnreadNew.set(peerId, 0);
    setPeerToUnread(peerToUnreadNew);
  };
  const processLeave = (peerId, groupId) => {
    const peerToGroupsNew = new Map(peerToGroups);
    const groups = peerToGroupsNew.has(peerId) ? peerToGroupsNew.get(peerId) : new Set();
    groups.delete(groupId);
    peerToGroupsNew.set(peerId, groups);
    if (groups.size == 0)
      peerToGroupsNew.delete(peerId);
    setPeerToGroups(peerToGroupsNew);

    const groupToPeersNew = new Map(groupToPeers);
    const peers = groupToPeersNew.has(groupId) ? groupToPeersNew.get(groupId) : new Set();
    peers.delete(peerId);
    groupToPeersNew.set(groupId, peers);
    if (peers.size == 0)
      groupToPeersNew.delete(groupId);
    setGroupToPeers(groupToPeersNew);

    const peerToUnreadNew = new Map(peerToUnread);
    peerToUnreadNew.delete(peerId);
    setPeerToUnread(peerToUnreadNew);
  };

  useEffect(() => {
    if (lastWebSocketMessage) {
      const data = lastWebSocketMessage.data;
      const message = JSON.parse(data);
      console.log(message);
      if (message.messageType) {
        if (message.messageType == 'JOIN') {
          const peerId = message.peerId;
          const groupId = message.groupId;
          processJoin(peerId, groupId);

          const object = {
            type: 'NAME',
            name: name,
          };
          const json = JSON.stringify(object);
          const encoded = new TextEncoder().encode(json)
          let data = ''
          for (let i = 0; i < encoded.length; i++) data += String.fromCodePoint(encoded.at(i))
          const base64 = window.btoa(data)
          sendWebSocketMessage(
            JSON.stringify({
              messageType: 'DATA',
              peerId: peerId,
              data: base64,
            }),
          )

        } else if (message.messageType == 'LEAVE') {
          const peerId = message.peerId;
          const groupId = message.groupId;
          processLeave(peerId, groupId);
        } else if (message.messageType == 'DATA') {
          const peerId = message.peerId;
          const base64 = message.data;
          const ascii = window.atob(base64);
          const array = Uint8Array.from(ascii, (c) => c.charCodeAt(0));
          const json = new TextDecoder().decode(array);
          const object = JSON.parse(json);
          if (object.type == 'CHAT') {
            const string = object.message;
            const messageObject = {
              received: true,
              peerId: peerId,
              message: string,
              timestamp: new Date(),
            };
            if (!location.pathname.endsWith(peerId)) {
              setPeerToUnread(peerToUnread => {
                const peerToUnreadNew = new Map(peerToUnread);
                peerToUnreadNew.set(peerId, peerToUnreadNew.get(peerId) + 1);
                return peerToUnreadNew;
              });
            }
            setMessages((messages) => [...messages, messageObject])
          } else if (object.type == 'NAME') {
            const name = object.name;
            setPeerToName(peerToName => {
              const peerToNameNew = new Map(peerToName);
              peerToNameNew.set(peerId, name);
              return peerToNameNew;
            });
          }
        }
      }
    }
  }, [lastWebSocketMessage])

  const sendMessage = (peerId, string) => {
    const object = {
      type: 'CHAT',
      message: string,
    };
    const json = JSON.stringify(object);
    const encoded = new TextEncoder().encode(json);
    let data = ''
    for (let i = 0; i < encoded.length; i++) data += String.fromCodePoint(encoded.at(i))
    const base64 = window.btoa(data)
    sendWebSocketMessage(
      JSON.stringify({
        messageType: 'DATA',
        peerId: peerId,
        data: base64,
      }),
    )
    const messageObject = {
      received: false,
      peerId: peerId,
      message: string,
      timestamp: new Date(),
    };
    setMessages((messages) => [...messages, messageObject])
  };

  useEffect(() => {
    if (webSocketState == ReadyState.OPEN) {
      groups.forEach(group => {
        getUuidFromString(group).then(uuid => {
          const joinObject = {
            messageType: 'JOIN',
            groupId: uuid,
            groupRole: 'PEER',
          };
          console.log(joinObject);
          sendWebSocketMessage(
            JSON.stringify(joinObject),
          );
        })
      })
    }
  }, [webSocketState])

  const homeNavItem = {
    component: CNavItem,
    name: 'Home',
    to: '/home',
    badge: {
      color: webSocketState == -1 || webSocketUrl == null ? 'danger-gradient' : (webSocketState == 0 ? 'warning-gradient' : (webSocketState == 1 ? 'success-gradient' : 'warning-gradient')),
      text: <div
        style={{width: '65px'}}>{webSocketState == -1 || webSocketUrl == null ? 'offline' : (webSocketState == 0 ? 'connecting' : (webSocketState == 1 ? 'online' : 'connecting'))}</div>,
    },
    icon: <CIcon icon={cilHome} customClassName="nav-icon"/>,
  };
  const groupsNavItem = {
    component: CNavItem,
    name: 'Groups',
    to: '/groups',
    badge: {
      color: 'info-gradient',
      text: groups.length,
    },
    icon: <CIcon icon={cilGlobeAlt} customClassName="nav-icon"/>,
  };
  const peersNavItem = {
    component: CNavItem,
    name: 'Peers',
    to: '/peers',
    badge: {
      color: 'info-gradient',
      text: peerToGroups.size,
    },
    icon: <CIcon icon={cilPeople} customClassName="nav-icon"/>,
  };
  const chatNavTitle = {
    component: CNavTitle,
    name: 'Chat'
  }
  const p2pNavTitle = {
    component: CNavTitle,
    name: 'P2P State'
  }
  const chatNavItems = peers.map(peer => {
    const unread = peerToUnread.get(peer.peerId);
    const badge = unread ? {
      color: 'danger-gradient',
      text: unread,
    } : undefined;
    return {
      component: CNavItem,
      name: peerToName.has(peer.peerId) ? peerToName.get(peer.peerId) : peer.peerId.substr(30, 6),
      to: '/chat/' + peer.peerId,
      badge: badge,
      icon: <CIcon icon={cilCommentBubble} customClassName="nav-icon"/>,
    };
  })

  const navigation = [
    homeNavItem,
    p2pNavTitle,
    groupsNavItem,
    peersNavItem,
    chatNavTitle,
    ...chatNavItems,
  ];

  const dispatch = useDispatch()
  const sidebarShow = useSelector((state) => state.sidebarShow)
  return <>
    <CSidebar
      position="fixed"
      unfoldable={false}
      visible={sidebarShow}
      onVisibleChange={(visible) => {
        dispatch({type: 'set', sidebarShow: visible})
      }}
    >
      <CSidebarBrand className="d-none d-md-flex" to="/">
        <CIcon className="sidebar-brand-full" icon={logoNegative} height={35}/>
        <CIcon className="sidebar-brand-narrow" icon={sygnet} height={35}/>
      </CSidebarBrand>
      <CSidebarNav>
        <SimpleBar>
          <AppSidebarNav items={navigation}/>
        </SimpleBar>
      </CSidebarNav>
    </CSidebar>
    <div className="wrapper d-flex flex-column min-vh-100 bg-light dark:bg-transparent">
      <AppHeader/>
      <div className="body flex-grow-1 px-3">
        <CContainer fluid>
          <Routes>
            <Route path="/home" name="Home" element={
              <Home name={name}
                    url={url}
                    isConnected={webSocketUrl !== null ? true : false}
                    connect={(name, url) => {
                      setName(name);
                      setUrl(url);
                      getUuidFromString(name).then(uuid => {
                        const webSocketUrl = `${url}/ws/events/${uuid}`;
                        setWebSocketUrl(webSocketUrl);
                      });
                    }}a
                    disconnect={() => setWebSocketUrl(null)}/>
            }/>
            <Route path="/groups" name="Groups" element={
              <Groups groups={groups}
                      add={group => {
                        setGroups(groups => [...groups, group]);
                        getUuidFromString(group).then(uuid => {
                          const joinObject = {
                            messageType: 'JOIN',
                            groupId: uuid,
                            groupRole: 'PEER',
                          };
                          console.log(joinObject);
                          sendWebSocketMessage(
                            JSON.stringify(joinObject),
                          );
                        })
                      }}
                      del={(group, index) => {
                        const array = [];
                        groups.forEach(g => {
                          if (g != group) array.push(g);
                        })
                        setGroups(array);
                        getUuidFromString(group).then(uuid => {
                          const joinObject = {
                            messageType: 'LEAVE',
                            groupId: uuid,
                            groupRole: 'PEER',
                          };
                          console.log(joinObject);
                          sendWebSocketMessage(
                            JSON.stringify(joinObject),
                          );
                        })
                      }}
              />}/>
            <Route path="/peers" name="Peers" element={<Peers peerToGroups={peerToGroups}/>}/>
            <Route path="/chat" name="Chat">
              <Route path=":peerId" element={<Chat messages={messages}
                                                   sendMessage={sendMessage}
                                                   peerToName={peerToName}
                                                   didRead={peerId => {
                                                     setPeerToUnread(peerToUnread => {
                                                       const peerToUnreadNew = new Map(peerToUnread);
                                                       peerToUnreadNew.set(peerId, 0);
                                                       return peerToUnreadNew;
                                                     });
                                                   }}
                                                   clearChat={peerId => {
                                                     setMessages(messages => {
                                                       const array = [];
                                                       messages.forEach(message => {
                                                         if (message.peerId != peerId)
                                                           array.push(message);
                                                       });
                                                       return array;
                                                     });
                                                   }}
              />}/>
            </Route>
          </Routes>
        </CContainer>
      </div>
      <CFooter>
        <div>PapaChat v1.0.0 &copy; 2022 Pavel Cséfalvay</div>
      </CFooter>
    </div>
  </>
}
