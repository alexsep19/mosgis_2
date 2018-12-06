define ([], function () {

    $_DO.check_voc_organizations = function (e) {

        function croak () {
            w2alert ('Поиск по заданному условию не дал результатов.<br><br>Попробуйте ввести в поле поиска полный точное значение ОГРН[ИП].')
        }

        var grid = w2ui [e.target]

        function load (ogrn, label) {

            w2confirm (label + ' ' + ogrn + ' не найден. Запросить информацию в ГИС ЖКХ?').yes (function () {

                $_SESSION.set ('importing', 1)
                grid.lock ('Запрос в ГИС ЖКХ...', true)

                query ({type: 'voc_organizations', action: 'import', id: undefined}, {data: {ogrn: ogrn}}, function (d) {

darn (d)
                    
                    var clock = setInterval (function () {
                    
                        query ({type: 'out_soap_export_nsi_item', part: 'rq', id: d.id}, {data: {ogrn: ogrn}}, function (out_soap) {
darn (out_soap)                        
                            if (out_soap.id_status < 3) return
                            
                            clearInterval (clock)
                            
                            grid.request ('get')
                        
                        })

                    }, 1000)

                })

            })

        }

        var last = grid.last;                if (last.logic != 'OR') return croak ()

        var s = (last.search || '').trim (); if (!(parseInt (s) > 0)) return croak ()

        switch (s.length) {
            case 13: return load (s, 'ОГРН')
            case 15: return load (s, 'ОГРНИП')
            default: return croak ()
        }

    }

    $_DO.create_branch_voc_organizations = function (record) {

        var options = {}

        if (record.id_type == 1) {
            options = {
                uuid_org_parent: record.uuid,
                label_org_parent: record.label,
                ogrn: record.ogrn,
                inn : record.inn
            }
        }

        $_SESSION.set('record', options)

        use.block('voc_organization_branch_add_new')
    }

    return function (done) {        
        
        var layout = w2ui ['rosters_layout']
            
        if (layout) layout.unlock ('main')
        
        query ({type: 'voc_organizations', part: 'vocs', id: undefined}, {}, function (data) {
        
            add_vocabularies (data, data)
                
            $('body').data ('data', data)
                        
            done (data)
        
        }) 
                
    }
    
})