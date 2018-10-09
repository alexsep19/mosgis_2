define ([], function () {

    $_DO.update_charter_object_new = function (e) {

        var form = w2ui ['voc_user_form']

        var v = form.values ()
        
        if (!v.fiashouseguid) die ('fiashouseguid', 'Укажите, пожалуйста, адрес обслуживаемого дома')

        if (v.enddate) {
            if (v.enddate < v.startdate) die ('enddate', 'Окончание периода не может предшествовать его началу')
        }
        
        var grid = w2ui ['charter_objects_grid']
        
        v.uuid_charter = $_REQUEST.id

        var data = $('body').data ('data')

        query ({type: 'charter_objects', action: 'create', id: undefined}, {data: v}, function () {
        
            if (data.item.id_customer_type == 1) {
            
                reload_page ()
            
            }
            else {

                w2popup.close ()

                grid.reload (grid.refresh)

            }
            
        })

    }

    return function (done) {

        var data = clone ($('body').data ('data'))

        data.record = {
            id_reason: 1,
            ismanagedbycontract: 0,
            startdate: dt_dmy (data.item.date_),
        }
        
        done (data)

    }

})