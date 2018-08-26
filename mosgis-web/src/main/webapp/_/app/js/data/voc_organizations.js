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

                query ({type: 'voc_organizations', action: 'import', id: undefined}, {data: {ogrn: ogrn}}, function () {

                    setTimeout (function () {

                        grid.request ('get')

                    }, 5000)

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