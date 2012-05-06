package models;
/*
 * Licensed Materials - Property of IBM
 *
 * L-GHUS-8ERMM3
 *
 * (C) Copyright IBM Corp. 2002, 2011. All rights reserved.
 *
 * US Government Users Restricted Rights- Use, duplication or 
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.lotus.sametime.announcement.AnnouncementComp;
import com.lotus.sametime.announcementui.AnnouncementUIComp;
import com.lotus.sametime.awareness.AwarenessComp;
import com.lotus.sametime.awarenessui.list.AwarenessList;
import com.lotus.sametime.buddylist.BLComp;
import com.lotus.sametime.chatui.ChatUIComp;
import com.lotus.sametime.commui.CommUIComp;
import com.lotus.sametime.community.CommunityService;
import com.lotus.sametime.community.LoginEvent;
import com.lotus.sametime.community.LoginListener;
import com.lotus.sametime.community.STBase;
import com.lotus.sametime.conf.ConfComp;
import com.lotus.sametime.core.comparch.DuplicateObjectException;
import com.lotus.sametime.core.comparch.STSession;
import com.lotus.sametime.core.types.STUser;
import com.lotus.sametime.directory.DirectoryComp;
import com.lotus.sametime.filetransfer.FileTransferComp;
import com.lotus.sametime.filetransferui.FileTransferUIComp;
import com.lotus.sametime.im.ImComp;
import com.lotus.sametime.lookup.LookupComp;
import com.lotus.sametime.lookup.LookupService;
import com.lotus.sametime.lookup.ResolveEvent;
import com.lotus.sametime.lookup.ResolveListener;
import com.lotus.sametime.lookup.Resolver;
import com.lotus.sametime.names.NamesComp;
import com.lotus.sametime.places.PlacesComp;
import com.lotus.sametime.post.PostComp;
import com.lotus.sametime.resourceloader.ResourceLoaderComp;
import com.lotus.sametime.storage.StorageComp;
import com.lotus.sametime.token.TokenComp;

/**
 * @author Shiri
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class LiveNamesConsole extends Frame implements LoginListener,
		ResolveListener, WindowListener {

	private STSession m_session;
	private CommunityService m_comm;
	private AwarenessList m_awarenessList;

	private String m_host;
	private String m_user;
	private String m_pass;
	private String m_users;
	private List<STUser> users = new ArrayList<STUser>();

	public LiveNamesConsole(String host, String user, String pass, String users) {
		m_host = host;
		m_user = user;
		m_pass = pass;
		m_users = users;

		try {
			m_session = new STSession("LiveNamesApplet " + this);
			//m_session.loadAllComponents();
			new STBase(m_session);
			new ConfComp(m_session);
			new ImComp(m_session);
			new PlacesComp(m_session);
			new PostComp(m_session);
			new StorageComp(m_session);
			//new WihComp(m_session);
			new AwarenessComp(m_session);
			new DirectoryComp(m_session);
			new LookupComp(m_session);
			new TokenComp(m_session);
			new NamesComp(m_session);
			new FileTransferComp(m_session);
			new AnnouncementComp(m_session);
			new BLComp(m_session);

			new ResourceLoaderComp(m_session);
			new CommUIComp(m_session);
			new ChatUIComp(m_session);
			//new FileTransferUIComp(m_session);
			new AnnouncementUIComp(m_session);

			m_session.start();

			setLayout(new BorderLayout());
			m_awarenessList = new AwarenessList(m_session, true);
			add(m_awarenessList, BorderLayout.CENTER);

			pack();
			setVisible(true);

			login();
		} catch (DuplicateObjectException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Main
	 */
	public static void main(String[] args) {
		new LiveNamesConsole("messenger.daumcorp.com", "aaa",
				"bbb", "ls15,sooo");
	}

	/**
	 * Login to the community using the user name and password 
	 * parameters from the html.
	 */
	private void login() {
		m_comm = (CommunityService) m_session
				.getCompApi(CommunityService.COMP_NAME);
		m_comm.addLoginListener(this);
		m_comm.loginByPassword(m_host, m_user, m_pass.toCharArray());
	}

	/**
	 * Logged in event. Print logged in msg to console.
	 * Resolve the users to add to the awareness list. 
	 */
	public void loggedIn(LoginEvent event) {
		System.out.println("Logged In");
		m_awarenessList.addUser((STUser) event.getLogin().getMyUserInstance());

		LookupService lookup = (LookupService) m_session
				.getCompApi(LookupService.COMP_NAME);

		Resolver resolver = lookup.createResolver(false, // Return all matches.
				false, // Non-exhaustive lookup.
				true, // Return resolved users.
				false); // Do not return resolved groups.

		resolver.addResolveListener(this);

		String[] userNames = getUserNames();
		resolver.resolve(userNames);
	}

	/**
	 * Helper method to read a list of user names from the html 
	 * parameter 'watchedNames'.
	 */
	String[] getUserNames() {
		String users = m_users;

		StringTokenizer tokenizer = new StringTokenizer(users, ",");
		String[] userNames = new String[tokenizer.countTokens()];

		int i = 0;
		while (tokenizer.hasMoreTokens()) {
			userNames[i++] = tokenizer.nextToken();
		}
		return userNames;
	}

	/**
	 * Users resolved succesfuly event. An event will be generated for 
	 * each resolved user. Add the resolved user to the awareness list.
	 */
	public void resolved(ResolveEvent event) {
		System.out.println("resolved user : "
				+ ((STUser) event.getResolved()).getId());
		m_awarenessList.addUser((STUser) event.getResolved());
		STUser user = (STUser) event.getResolved();
		users.add(user);
	}

	/**
	 * Handle a resolve conflict event. Will be received in the case 
	 * that more then one match was found for a specified user name. 
	 * Add the users to the awareness list anyway.
	 */
	public void resolveConflict(ResolveEvent event) {
		STUser[] users = (STUser[]) event.getResolvedList();
		for (STUser user : users) {
			System.out.println("resolved conflict user : " + user.getId());
		}
		m_awarenessList.addUsers(users);
	}

	/**
	 * Resolve failed. No users are available to add to the list.
	 */
	public void resolveFailed(ResolveEvent event) {
	}

	/**
	 * Logged out event. Print logged out msg to console. Leave default
	 * behavior which will display a dialog box. 
	 */
	public void loggedOut(LoginEvent event) {
		System.out.println("Logged Out");
	}

	/**
	 * Applet destroyed. Logout, stop and unload the session.
	 */
	public void destroy() {
		m_comm.logout();
		m_session.stop();
		m_session.unloadSession();
	}

	/**
	 * @see java.awt.event.WindowListener#windowActivated(WindowEvent)
	 */
	public void windowActivated(WindowEvent e) {
	}

	/**
	 * @see java.awt.event.WindowListener#windowClosed(WindowEvent)
	 */
	public void windowClosed(WindowEvent e) {
	}

	/**
	 * @see java.awt.event.WindowListener#windowClosing(WindowEvent)
	 */
	public void windowClosing(WindowEvent e) {
		dispose();
		System.exit(0);
	}

	/**
	 * @see java.awt.event.WindowListener#windowDeactivated(WindowEvent)
	 */
	public void windowDeactivated(WindowEvent e) {
	}

	/**
	 * @see java.awt.event.WindowListener#windowDeiconified(WindowEvent)
	 */
	public void windowDeiconified(WindowEvent e) {
	}

	/**
	 * @see java.awt.event.WindowListener#windowIconified(WindowEvent)
	 */
	public void windowIconified(WindowEvent e) {
	}

	/**
	 * @see java.awt.event.WindowListener#windowOpened(WindowEvent)
	 */
	public void windowOpened(WindowEvent e) {
	}

}
