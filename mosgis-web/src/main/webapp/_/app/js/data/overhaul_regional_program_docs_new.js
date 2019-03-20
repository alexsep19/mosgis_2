define ([], function () {

    $_DO.update_overhaul_regional_program_docs_new = function (e) {

        var form = w2ui ['overhaul_regional_program_docs_new_form']

        var v = form.values ()
        
        if (!v.code_nsi_79) die ('code_nsi_79', 'Пожалуйста, укажите вид документа')
        if (!v.number_) die ('number_', 'Пожалуйста, укажите номер документа')
        if (!v.date_) die ('date_', 'Пожалуйстаб укажите, дату')    
        if (!v.fullname) die ('fullname', 'Пожалуйста, укажите наименование')
        if (!v.legislature) die ('legislature', 'Пожалуйста, укажите орган власти, принявший документ')
        
        var tia = {type: 'overhaul_regional_program_documents'}
        tia.id = form.record.id
        tia.action = tia.id ? 'update' : 'create'
        
        var done = reload_page

        var grid = w2ui ['overhaul_regional_program_docs_grid']
        
        var data = clone ($('body').data ('data'))

        query (tia, {data: v}, function (data) {
        
            w2popup.close ()
            
            if (data.id) w2confirm ('Перейти на страницу документа?').yes (function () {openTab ('/overhaul_regional_program_doc/' + data.id)})
            
            grid.reload (grid.refresh)
            
        })

    }

    return function (done) {

        var data = clone ($('body').data ('data'))
        
        data.record = $_SESSION.delete ('record')
        
        done (data)

    }

})