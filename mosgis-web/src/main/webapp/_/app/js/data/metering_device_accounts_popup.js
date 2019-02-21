define ([], function () {

    $_DO.update_metering_device_accounts_popup = function (e) {

        var form = w2ui ['org_work_form']

        var v = form.values ()
        
        if (!v.workname) die ('workname', 'Укажите, пожалуйста, наименование услуги')
        if (!v.code_vc_nsi_56) die ('code_vc_nsi_56', 'Укажите, пожалуйста, вид работ')
        if (!v.stringdimensionunit) die ('stringdimensionunit', 'Укажите, пожалуйста, единицу измерения')
        
        var vc_okei = $('body').data ('data').vc_okei
        for (var id in vc_okei) {
            if (vc_okei [id] != v.stringdimensionunit) continue
            v.okei = id
            delete v.stringdimensionunit
            break
        }
        
        if (!v.okei && !confirm ('Вы уверены, что Общероссийский классификатор единиц измерения неприменим для определения объёма данной услуги?')) return $('#stringdimensionunit').val ('').focus ()
        
        v.code_vc_nsi_67 = w2ui ['code_vc_nsi_67_grid'].getSelection ()
        
        if (!v.code_vc_nsi_67.length) die ('foo', 'Укажите, пожалуйста, по крайней мере одну обязательную работу')
                
        var tia = {type: 'org_works'}
        tia.id = form.record.id
        tia.action = tia.id ? 'update' : 'create'
        
        var done = reload_page

        var grid = w2ui ['org_works_grid']

        query (tia, {data: v}, function () {
        
            w2popup.close ()
            
            grid.reload (grid.refresh)
            
        })

    }

    return function (done) {

        var data = clone ($('body').data ('data'))
        
        var it = data.item
        
        var v = {fiashouseguid: null, uuid_premise: null}
        
        for (k in v) v [k] = it [k]
                
        query ({type: 'account_items', id: null}, {data: v, offset: 0, limit: 10000}, function (d) {
                
            data.account_items = dia2w2uiRecords (d.root)
            
            $.each (data.account_items, function () {this.label = this ['ind.label'] || this ['org.label']})
            
darn (data.account_items)                
            data.record = {}

            done (data)
        
        })        

    }

})