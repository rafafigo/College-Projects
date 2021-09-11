from selenium import webdriver
from selenium.common.exceptions import TimeoutException
from selenium.webdriver.chrome.options import Options


def startDriver():
    chrome_options = Options()
    chrome_options.add_argument("--headless")
    chrome_options.add_argument("--no-sandbox")
    drv = webdriver.Chrome(chrome_options=chrome_options)
    drv.set_page_load_timeout(5)
    drv.set_script_timeout(5)
    return drv


def getScript(fn, sArgs=None):
    with open(fn, 'r') as fd:
        scr = fd.read()
    if isinstance(sArgs, dict):
        for sK, sV in sArgs.items():
            scr = scr.replace("[{}]".format(sK), sV)
    return scr


def getSessionCookie(session):
    return {
        "name": "session",
        "value": session.cookies.get("session")
    }


def runScript(srcURL, trgURL, session):
    drv = startDriver()
    try:
        drv.get(srcURL)
        drv.add_cookie(getSessionCookie(session))
        drv.get(trgURL)
    except TimeoutException as e:
        print("Could not Connect to {} & {}".format(srcURL, trgURL))
        raise e
    except Exception as e:
        raise e
    finally:
        drv.close()


def runAlert(srcURL, trgURL, session, alrText):
    drv = startDriver()
    try:
        drv.get(srcURL)
        drv.add_cookie(getSessionCookie(session))
        drv.get(trgURL)
        alr = drv.switch_to.alert
        assert alrText in alr.text
        alr.accept()
    except TimeoutException as e:
        print("Could not Connect to {} & {}".format(srcURL, trgURL))
        raise e
    except Exception as e:
        raise e
    finally:
        drv.close()
