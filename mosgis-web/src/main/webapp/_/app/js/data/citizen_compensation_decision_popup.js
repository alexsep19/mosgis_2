define ([], function () {

    var form_name = 'citizen_compensation_decision_popup_form'

    $_DO.update_citizen_compensation_decision_popup = function (e) {

        var form = w2ui [form_name]

        var v = form.values ()
        v.uuid_cit_comp = $_REQUEST.id

        if (!v.decisiondate)    die ('decisiondate', 'Укажите, пожалуйста, дату решения')
        if (!v.code_vc_nsi_301) die ('code_vc_nsi_301', 'Укажите, пожалуйста, тип решения')

        var tia = {type: 'citizen_compensation_decisions'}
        tia.id = form.record.id
        tia.action = tia.id ? 'update' : 'create'
        
        var grid = w2ui ['citizen_compensation_decisions_grid']

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