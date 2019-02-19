define([], function () {

    $_DO.create_supply_resource_contract_objects = function (e) {

        use.block('supply_resource_contract_objects_new')
    }

    return function (done) {

        w2ui ['topmost_layout'].unlock('main')

        var data = clone($('body').data('data'))

        done(data)

    }

})