define ([], function () {

    $_DO.update_tarif_diffs_popup = function (e) {

        var form = w2ui ['tarif_diff_form']

        var v = form.values ()

        if(!v.id) {
            if (!v.code_diff) die ('code_diff', 'Укажите, пожалуйста, критерий дифференциации')
        }

        var field    = 'value' + v.id_type.toLowerCase()
        field = ['Fias', 'OKTMO', 'Enumeration'].indexOf(v.id_type) != -1? v.id_type.toLowerCase() : field
        var field_to = field + '_to'
        var value    = v[field]
        var value_to = v[field_to]

        if(!v.operator && ['Real', 'Integer', 'Enumeration', 'Date', 'Year'].indexOf(v.id_type) != -1) {
            die('operator', 'Укажите, пожалуйста, оператор')
        }

        if (!value && value !== 0) die(field, 'Укажите, пожалуйста, значение критерия дифференциации')

        if (/Range/.test(v.operator) && !value_to && v.id_type != 'Enumeration') {
            die(field_to, 'Укажите, пожалуйста, конец диапазона значения критерия дифференциации')
        }

        switch (v.id_type) {
            case 'Year':
                if (!(1920 <= value && value <= 2050))
                    die('year', 'В поле "Год" должен быть указан год между 1920 и 2050')
                break;
            case 'Multiline':
                if (!v.label) die('label', 'Укажите, пожалуйста, наименование значения критерия дифференциации')
        }

        if (v.valuemultiline) {
            v.valuestring = v.valuemultiline
            delete v.valuemultiline
        }

        var tia = {type: 'tarif_diffs'}
        tia.id = form.record.id
        tia.action = tia.id ? 'update' : 'create'
        
        var grid = w2ui ['tarif_diffs_grid']

        query (tia, {data: v}, function () {
        
            w2popup.close ()
            
            grid.reload (grid.refresh)
            
        })

    }

    return function (done) {

        var data = clone ($('body').data ('data'))


        data.record = $_SESSION.delete ('record') || {}

        switch (data.record.id_type) {
            case 'Multiline':
                data.record.valuemultiline = data.record.valuestring
                delete data.record.valuestring
                break
        }

        data._can = data.record._can = {
            update: 1
        }

        done(data)

    }

})