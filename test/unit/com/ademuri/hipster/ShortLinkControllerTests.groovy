package com.ademuri.hipster



import org.junit.*
import grails.test.mixin.*

@TestFor(ShortLinkController)
@Mock(ShortLink)
class ShortLinkControllerTests {

    def populateValidParams(params) {
        assert params != null
        // TODO: Populate valid properties like...
        //params["name"] = 'someValidName'
    }

    void testIndex() {
        controller.index()
        assert "/shortLink/list" == response.redirectedUrl
    }

    void testList() {

        def model = controller.list()

        assert model.shortLinkInstanceList.size() == 0
        assert model.shortLinkInstanceTotal == 0
    }

    void testCreate() {
        def model = controller.create()

        assert model.shortLinkInstance != null
    }

    void testSave() {
        controller.save()

        assert model.shortLinkInstance != null
        assert view == '/shortLink/create'

        response.reset()

        populateValidParams(params)
        controller.save()

        assert response.redirectedUrl == '/shortLink/show/1'
        assert controller.flash.message != null
        assert ShortLink.count() == 1
    }

    void testShow() {
        controller.show()

        assert flash.message != null
        assert response.redirectedUrl == '/shortLink/list'

        populateValidParams(params)
        def shortLink = new ShortLink(params)

        assert shortLink.save() != null

        params.id = shortLink.id

        def model = controller.show()

        assert model.shortLinkInstance == shortLink
    }

    void testEdit() {
        controller.edit()

        assert flash.message != null
        assert response.redirectedUrl == '/shortLink/list'

        populateValidParams(params)
        def shortLink = new ShortLink(params)

        assert shortLink.save() != null

        params.id = shortLink.id

        def model = controller.edit()

        assert model.shortLinkInstance == shortLink
    }

    void testUpdate() {
        controller.update()

        assert flash.message != null
        assert response.redirectedUrl == '/shortLink/list'

        response.reset()

        populateValidParams(params)
        def shortLink = new ShortLink(params)

        assert shortLink.save() != null

        // test invalid parameters in update
        params.id = shortLink.id
        //TODO: add invalid values to params object

        controller.update()

        assert view == "/shortLink/edit"
        assert model.shortLinkInstance != null

        shortLink.clearErrors()

        populateValidParams(params)
        controller.update()

        assert response.redirectedUrl == "/shortLink/show/$shortLink.id"
        assert flash.message != null

        //test outdated version number
        response.reset()
        shortLink.clearErrors()

        populateValidParams(params)
        params.id = shortLink.id
        params.version = -1
        controller.update()

        assert view == "/shortLink/edit"
        assert model.shortLinkInstance != null
        assert model.shortLinkInstance.errors.getFieldError('version')
        assert flash.message != null
    }

    void testDelete() {
        controller.delete()
        assert flash.message != null
        assert response.redirectedUrl == '/shortLink/list'

        response.reset()

        populateValidParams(params)
        def shortLink = new ShortLink(params)

        assert shortLink.save() != null
        assert ShortLink.count() == 1

        params.id = shortLink.id

        controller.delete()

        assert ShortLink.count() == 0
        assert ShortLink.get(shortLink.id) == null
        assert response.redirectedUrl == '/shortLink/list'
    }
}
