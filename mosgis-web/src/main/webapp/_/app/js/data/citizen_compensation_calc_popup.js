define ([], function () {

    var form_name = 'citizen_compensation_calc_popup_form'

    $_DO.update_citizen_compensation_calc_popup = function (e) {

        var form = w2ui [form_name]

        var v = form.values ()
        v.uuid_cit_comp = $_REQUEST.id

        if (!v.periodfrom)      die ('periodfrom', 'Укажите, пожалуйста, дату начала расчета')
        if (!v.periodto)        die ('periodto', 'Укажите, пожалуйста, дату окончания расчета')
        if (!v.calculationdate) die ('calculationdate', 'Укажите, пожалуйста, дату расчета')
        if (!v.compensationsum) die ('compensationsum', 'Укажите, пожалуйста, размер компенсации расходов')

        var tia = {type: 'citizen_compensation_calcs'}
        tia.id = form.record.id
        tia.action = tia.id ? 'update' : 'create'
        
        var grid = w2ui ['citizen_compensation_calcs_grid']

        query (tia, {data: v}, function () {
        
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