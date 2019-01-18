define ([], function () {

    var name = 'reporting_period_unplanned_works_popup_form'

    $_DO.open_orgs_reporting_period_unplanned_works_popup = function (e) {
    
        var f = w2ui [name]    

        var saved = {
            data: clone ($('body').data ('data')),
            record: clone (f.record)
        }
                
        $('body').data ('voc_organizations_popup.callback', function (r) {
        
            function done () {

                $('body').data ('data', saved.data)

                $_SESSION.set ('record', saved.record)

                use.block ('reporting_period_unplanned_works_popup')

            }

            if (!r) return done ()
            
            query ({type: 'voc_organizations', id: r.uuid}, {}, function (d) {
            
                var nsi_20 = {}
                
                $.each (d.vc_orgs_nsi_20, function () {nsi_20 [parseInt (this.code)] = 1})
                
                if (!nsi_20 [2]) {
                    alert ('Указанная организация не зарегистрирована в ГИС ЖКХ как ресурсоснабжающая')
                }
                else {
                    saved.record.organizationguid = r.uuid
                    saved.record.label_organizationguid = r.label
                }

                done ()

            })
                                    
        })
        
        use.block ('voc_organizations_popup')

    }

    $_DO.update_reporting_period_unplanned_works_popup = function (e) {

        var form = w2ui [name]

        var v = form.values ()

        if (!v.uuid_org_work) die ('uuid_org_work', 'Укажите, пожалуйста, вид услуги')
        if (!(v.price > 0)) die ('price', 'Укажите, пожалуйста, цену')
        if (!(v.amount > 0)) die ('amount', 'Укажите, пожалуйста, объём оказанных услуг')
        if (!(v.count > 0)) die ('count', 'Укажите, пожалуйста, количество оказанных услуг')
        
        if (v.code_vc_nsi_56 == 3) {
            if (!v.code_vc_nsi_57) die ('code_vc_nsi_57', 'Укажите, пожалуйста, объект аварии')
            if (!v.accidentreason) die ('accidentreason', 'Укажите, пожалуйста, причину аварии')
            if (v.code_vc_nsi_57 > 1 && !v.code_vc_nsi_3)  die ('code_vc_nsi_3', 'Укажите, пожалуйста, вид КУ')
        }
        else {
            v.code_vc_nsi_57 = null
            v.accidentreason = ""
        }

        if (v.code_vc_nsi_56 == 5) {
            if (!v.code_vc_nsi_3)  die ('code_vc_nsi_3', 'Укажите, пожалуйста, вид КУ')
            if (!v.organizationguid)  die ('organizationguid', 'Укажите, пожалуйста, поставщика коммунального ресурса')
        }
        else {
            v.organizationguid = null
        }

        switch (parseInt (v.code_vc_nsi_56)) {
            case 3:
            case 5:
                break
            default:
                v.code_vc_nsi_3 = null
        }
        
        if (v.comment_ == null) v.comment_ = ""

        var tia = {type: 'unplanned_works'}
        tia.id = form.record.id
        tia.action = tia.id ? 'update' : 'create'
        
        var grid = w2ui ['reporting_period_unplanned_works_grid']
        
        grid.lock ()
return
        query (tia, {data: v}, function () {
        
            grid.unlock ()

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