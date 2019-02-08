define ([], function () {

    $_DO.update_infrastructure_net_pieces_popup = function (e) {

        var form = w2ui ['infrastructure_net_pieces_popup_form']

        var data = clone ($('body').data ('data'))
        var v = form.values ()

        var reg_number = /^\d+(\.\d{1,3})?$/

        if (data.item.code_vc_nsi_33 == '4.5' && !v.code_vc_nsi_36) die ('code_vc_nsi_36', 'Укажите, пожалуйста, уровень давления газопровода')
        if (data.item.code_vc_nsi_33 == '5.4' && !v.code_vc_nsi_45) die ('code_vc_nsi_45', 'Укажите, пожалуйста, уровень напряжения')
        
        if (!v.diameter) die ('diameter', 'Укажите, пожалуйста, диаметр')
        if (!reg_number.test (v.diameter)) die ('diameter', 'Указано неверное значение диаметра')

        if (!v.length) die ('length', 'Укажите, пожалуйста, длину')
        if (!reg_number.test (v.length)) die ('length', 'Указана неверная длина')

        if (!v.needreplaced) die ('needreplaced', 'Укажите, пожалуйста, длину участка, нуждающегося в замене')
        if (!reg_number.test (v.needreplaced)) die ('needreplaced', 'Указана неверная длина участка, нуждающегося в замене')

        if (!v.wearout) die ('wearout', 'Укажите, пожалуйста, уровень износа')
        else {
            var wearout = parseFloat (v.wearout)
            if (isNaN (wearout) || wearout < 0 || wearout > 100) die ('wearout', 'Указано неверное значения уровня износа')
            v.wearout = wearout
        }

        v.uuid_oki = $_REQUEST.id
        
        var tia = {type: 'infrastructure_net_pieces'}
        tia.id = form.record.id
        tia.action = tia.id ? 'update' : 'create'
        
        var done = reload_page

        var grid = w2ui ['infrastructure_net_pieces_grid']

        query (tia, {data: v}, function (data) {
        
            w2popup.close ()
            grid.reload (grid.refresh)
            
        })

    }

    return function (done) {

        var data = clone ($('body').data ('data'))
        
        data.record = $_SESSION.delete ('record')
        
        done (data)

    }

})